package tv.trakt.trakt.core.user.features.profile.sections.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.core.lists.model.ListsMediaFilter
import tv.trakt.trakt.core.lists.model.ListsMediaFilter.MEDIA
import tv.trakt.trakt.core.lists.model.ListsMediaFilter.MOVIES
import tv.trakt.trakt.core.lists.model.ListsMediaFilter.SHOWS
import tv.trakt.trakt.core.user.features.profile.model.FavoriteItem
import tv.trakt.trakt.core.user.features.profile.sections.favorites.filters.GetFavoritesFilterUseCase
import tv.trakt.trakt.core.user.usecase.lists.LoadUserFavoritesUseCase

internal class ProfileFavoritesViewModel(
    private val loadFavoritesUseCase: LoadUserFavoritesUseCase,
    private val getFilterUseCase: GetFavoritesFilterUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = ProfileFavoritesState()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadDataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadData()
    }

    fun loadData(ignoreErrors: Boolean = false) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val filter = loadFilter()
                val localItems = when (filter) {
                    MEDIA -> loadFavoritesUseCase.loadLocalAll()
                    SHOWS -> loadFavoritesUseCase.loadLocalShows()
                    MOVIES -> loadFavoritesUseCase.loadLocalMovies()
                }.take(LISTS_SECTION_LIMIT)

                if (localItems.isNotEmpty()) {
                    itemsState.update {
                        localItems.toImmutableList()
                    }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    when (filter) {
                        MEDIA -> loadFavoritesUseCase.loadAll()
                        SHOWS -> loadFavoritesUseCase.loadShows()
                        MOVIES -> loadFavoritesUseCase.loadMovies()
                    }
                        .take(LISTS_SECTION_LIMIT)
                        .toImmutableList()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.w(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun loadFilter(): ListsMediaFilter {
        val filter = getFilterUseCase.getFilter()
        filterState.update { filter }
        return filter
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<FavoriteItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        }

        return false
    }

    fun setFilter(newFilter: ListsMediaFilter) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            getFilterUseCase.setFilter(newFilter)
            loadFilter()
            loadData()
        }
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

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ProfileFavoritesState> = combine(
        loadingState,
        itemsState,
        filterState,
        navigateShow,
        navigateMovie,
        errorState,
        userState,
    ) { states ->
        ProfileFavoritesState(
            loading = states[0] as LoadingState,
            items = states[1] as ImmutableList<FavoriteItem>?,
            filter = states[2] as ListsMediaFilter,
            navigateShow = states[3] as TraktId?,
            navigateMovie = states[4] as TraktId?,
            error = states[5] as Exception?,
            user = states[6] as User?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
