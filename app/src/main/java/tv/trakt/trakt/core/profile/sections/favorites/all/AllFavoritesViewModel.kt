package tv.trakt.trakt.core.profile.sections.favorites.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.favorites.FavoritesUpdates
import tv.trakt.trakt.core.favorites.FavoritesUpdates.Source.CONTEXT_SHEET
import tv.trakt.trakt.core.favorites.FavoritesUpdates.Source.DETAILS
import tv.trakt.trakt.core.favorites.FavoritesUpdates.Source.RATE_PROMPT
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.main.model.MediaMode.MEDIA
import tv.trakt.trakt.core.main.model.MediaMode.MOVIES
import tv.trakt.trakt.core.main.model.MediaMode.SHOWS
import tv.trakt.trakt.core.user.usecases.lists.LoadUserFavoritesUseCase

@OptIn(FlowPreview::class)
internal class AllFavoritesViewModel(
    private val loadFavoritesUseCase: LoadUserFavoritesUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val favoritesUpdates: FavoritesUpdates,
    private val sessionManager: SessionManager,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = AllFavoritesState()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val sortingState = MutableStateFlow(initialState.sorting)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadDataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadUser()
        loadData()
        observeData()

        analytics.logScreenView(
            screenName = "all_favorites",
        )
    }

    private fun observeData() {
        merge(
            favoritesUpdates.observeUpdates(DETAILS),
            favoritesUpdates.observeUpdates(CONTEXT_SHEET),
            favoritesUpdates.observeUpdates(RATE_PROMPT),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(
                    ignoreErrors = true,
                    localOnly = true,
                )
            }
            .launchIn(viewModelScope)
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun loadData(
        ignoreErrors: Boolean = false,
        localOnly: Boolean = false,
    ) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val filter = filterState.value ?: MEDIA
                val sorting = sortingState.value

                val localItems = when (filter) {
                    MEDIA -> loadFavoritesUseCase.loadLocalAll(sort = sorting)
                    SHOWS -> loadFavoritesUseCase.loadLocalShows(sort = sorting)
                    MOVIES -> loadFavoritesUseCase.loadLocalMovies(sort = sorting)
                }

                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems.toImmutableList() }
                    loadingState.update { Done }
                    if (localOnly) {
                        return@launch
                    }
                } else {
                    loadingState.update { Loading }
                }

                itemsState.update {
                    when (filter) {
                        MEDIA -> loadFavoritesUseCase.loadAll(sort = sorting)
                        SHOWS -> loadFavoritesUseCase.loadShows(sort = sorting)
                        MOVIES -> loadFavoritesUseCase.loadMovies(sort = sorting)
                    }.toImmutableList()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<FavoriteItem>().toImmutableList()
            }
            loadingState.update { Done }
            return true
        }

        return false
    }

    fun setFilter(newFilter: MediaMode) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            filterState.update { newFilter }
            loadData(localOnly = true)
        }
    }

    fun setSorting(newSorting: Sorting) {
        if (newSorting == sortingState.value) {
            return
        }

        sortingState.update {
            it.copy(
                type = newSorting.type,
                order = newSorting.order,
            )
        }

        loadData(localOnly = true)
    }

    fun navigateToShow(show: Show) {
        if (navigateShow.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            navigateShow.update { show.ids.trakt }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            movieLocalDataSource.upsertMovies(listOf(movie))
            navigateMovie.update { movie.ids.trakt }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
    }

    override fun onCleared() {
        loadDataJob?.cancel()
        loadDataJob = null

        processingJob?.cancel()
        processingJob = null

        super.onCleared()
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        loadingState,
        itemsState,
        filterState,
        sortingState,
        navigateShow,
        navigateMovie,
        userState,
        errorState,
    ) { state ->
        AllFavoritesState(
            loading = state[0] as LoadingState,
            items = state[1] as? ImmutableList<FavoriteItem>,
            filter = state[2] as? MediaMode,
            sorting = state[3] as Sorting,
            navigateShow = state[4] as? TraktId,
            navigateMovie = state[5] as? TraktId,
            user = state[6] as? User,
            error = state[7] as? Exception,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
