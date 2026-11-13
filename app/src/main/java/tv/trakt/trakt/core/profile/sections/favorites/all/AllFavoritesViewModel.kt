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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.core.favorites.FavoriteItem.MovieItem
import tv.trakt.trakt.common.core.favorites.FavoriteItem.ShowItem
import tv.trakt.trakt.common.core.favorites.getFavoriteSorting
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserFavoritesUseCase
import tv.trakt.trakt.common.core.user.usecases.ratings.LoadUserRatingsUseCase
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.analytics.logListSort
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaMode.Media
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.helpers.navigation.MediaNavigationDelegate
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
internal class AllFavoritesViewModel(
    private val loadFavoritesUseCase: LoadUserFavoritesUseCase,
    private val loadRatingsUseCase: LoadUserRatingsUseCase,
    private val dataChanges: DataChanges,
    private val sessionManager: SessionManager,
    private val filterManager: GlobalFilterManager,
    private val collectionStateProvider: CollectionStateProvider,
    private val analytics: Analytics,
    navigationFactory: MediaNavigationDelegate.Factory,
) : ViewModel() {
    private val initialState = AllFavoritesState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow<MediaMode?>(filterManager.getFilter().mode)
    private val sortingState = MutableStateFlow(initialState.sorting)
    private val navigation = navigationFactory.create(
        scope = viewModelScope,
        isBusy = { processingJob?.isActive == true },
    )
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadDataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadData()
        observeData()

        analytics.logScreenView(
            screenName = "all_favorites",
        )
    }

    private fun observeData() {
        dataChanges.observe(Kind.Favorites)
            .debounce(200.milliseconds)
            .onEach {
                loadData(
                    ignoreErrors = true,
                    localOnly = true,
                )
            }
            .launchIn(viewModelScope)

        collectionStateProvider
            .launchIn(viewModelScope)
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

                val filter = filterState.value ?: Media
                val sorting = sortingState.value

                val (showsRatings, moviesRatings) = loadRatingsUseCase.loadAllIfNeeded()

                val localItems = when (filter) {
                    Media -> loadFavoritesUseCase.loadLocalAll()
                    Shows -> loadFavoritesUseCase.loadLocalShows()
                    Movies -> loadFavoritesUseCase.loadLocalMovies()
                }.map {
                    when (it) {
                        is ShowItem -> it.copy(userRating = showsRatings[it.id])
                        is MovieItem -> it.copy(userRating = moviesRatings[it.id])
                    }
                }.sortedWith(
                    getFavoriteSorting(sorting),
                )

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
                        Media -> loadFavoritesUseCase.loadAll(sort = sorting)
                        Shows -> loadFavoritesUseCase.loadShows(sort = sorting)
                        Movies -> loadFavoritesUseCase.loadMovies(sort = sorting)
                    }.map {
                        when (it) {
                            is ShowItem -> it.copy(userRating = showsRatings[it.id])
                            is MovieItem -> it.copy(userRating = moviesRatings[it.id])
                        }
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
        analytics.lists.logListSort(newSorting)

        loadData(localOnly = true)
    }

    fun navigateToShow(show: Show) {
        navigation.toShow(show)
    }

    fun navigateToMovie(movie: Movie) {
        navigation.toMovie(movie)
    }

    fun clearNavigation() {
        navigation.clear()
    }

    override fun onCleared() {
        loadDataJob?.cancel()
        loadDataJob = null
        processingJob?.cancel()
        processingJob = null
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        loadingState,
        itemsState,
        filterState,
        sortingState,
        navigation.show,
        navigation.movie,
        sessionManager.observeProfile(),
        errorState,
        collectionStateProvider.stateFlow,
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
            collection = state[8] as UserCollectionState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
