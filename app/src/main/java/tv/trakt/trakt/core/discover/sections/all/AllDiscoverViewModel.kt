package tv.trakt.trakt.core.discover.sections.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.interleave
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverItem.MovieItem
import tv.trakt.trakt.core.discover.model.DiscoverItem.ShowItem
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.sections.all.navigation.DiscoverDestination
import tv.trakt.trakt.core.discover.sections.all.usecases.GetAllDiscoverMoviesUseCase
import tv.trakt.trakt.core.discover.sections.all.usecases.GetAllDiscoverShowsUseCase
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.home.sections.recommended.usecase.HideRecommendedMovieUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.HideRecommendedShowUseCase

@Suppress("UNCHECKED_CAST")
internal class AllDiscoverViewModel(
    savedStateHandle: SavedStateHandle,
    analytics: Analytics,
    private val sessionManager: SessionManager,
    private val filterManager: GlobalFilterManager,
    private val getShowsUseCase: GetAllDiscoverShowsUseCase,
    private val getMoviesUseCase: GetAllDiscoverMoviesUseCase,
    private val hideRecommendedShowUseCase: HideRecommendedShowUseCase,
    private val hideRecommendedMovieUseCase: HideRecommendedMovieUseCase,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<DiscoverDestination>()
    private val initialState = AllDiscoverState()

    private val userState = MutableStateFlow(initialState.user)
    private val filterState = MutableStateFlow(filterManager.getFilter())
    private val typeState = MutableStateFlow(destination.source)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var pages = 1
    private var hasMoreData = false

    init {
        loadUser()
        loadInitialData()

        observeFilters()
        observeData()

        analytics.logScreenView(
            screenName = "all_discover_${destination.source.name.lowercase()}",
        )
    }

    private fun observeFilters() {
        filterManager.observeFilter()
            .onEach { value ->
                filterState.update { value }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun observeData() {
        collectionStateProvider
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

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                loadLocalData()

                coroutineScope {
                    val showsAsync = async {
                        getShowsUseCase.getShows(
                            source = destination.source,
                            filters = filterState.value,
                            skipLocal = true,
                        )
                    }
                    val moviesAsync = async {
                        getMoviesUseCase.getMovies(
                            source = destination.source,
                            filters = filterState.value,
                            skipLocal = true,
                        )
                    }

                    val shows = if (filterState.value.mode.isMediaOrShows) showsAsync.await() else emptyList()
                    val movies = if (filterState.value.mode.isMediaOrMovies) moviesAsync.await() else emptyList()

                    val initialData = listOf(shows, movies)
                        .interleave()

                    itemsState
                        .update {
                            initialData.toImmutableList()
                        }.also {
                            hasMoreData = initialData.isNotEmpty()
                        }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                pages = 1
                hasMoreData = false

                coroutineScope {
                    val showsAsync = async {
                        getShowsUseCase.getShows(
                            source = destination.source,
                            filters = filterState.value,
                            skipLocal = true,
                        )
                    }
                    val moviesAsync = async {
                        getMoviesUseCase.getMovies(
                            source = destination.source,
                            filters = filterState.value,
                            skipLocal = true,
                        )
                    }

                    val shows = if (filterState.value.mode.isMediaOrShows) showsAsync.await() else emptyList()
                    val movies = if (filterState.value.mode.isMediaOrMovies) moviesAsync.await() else emptyList()

                    itemsState.update {
                        listOf(shows, movies)
                            .interleave()
                            .toImmutableList()
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private suspend fun loadLocalData() {
        return coroutineScope {
            val localShowsAsync = async { getShowsUseCase.getLocalShows(destination.source) }
            val localMoviesAsync = async { getMoviesUseCase.getLocalMovies(destination.source) }

            val localShows = if (filterState.value.mode.isMediaOrShows) localShowsAsync.await() else emptyList()
            val localMovies = if (filterState.value.mode.isMediaOrMovies) localMoviesAsync.await() else emptyList()

            itemsState
                .update {
                    listOf(localShows, localMovies)
                        .interleave()
                        .toImmutableList()
                }.also {
                    if (localShows.isEmpty() && localMovies.isEmpty()) {
                        loadingState.update { Loading }
                    }
                }
        }
    }

    fun loadMoreData() {
        if (itemsState.value.isNullOrEmpty() || !hasMoreData) {
            return
        }
        if (loadingMoreState.value.isLoading || loadingState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingMoreState.update { Loading }

                coroutineScope {
                    val showsAsync = async {
                        getShowsUseCase.getShows(
                            source = destination.source,
                            page = pages + 1,
                            filters = filterState.value,
                            skipLocal = true,
                        )
                    }

                    val moviesAsync = async {
                        getMoviesUseCase.getMovies(
                            source = destination.source,
                            page = pages + 1,
                            filters = filterState.value,
                            skipLocal = true,
                        )
                    }

                    val shows = if (filterState.value.mode.isMediaOrShows) showsAsync.await() else emptyList()
                    val movies = if (filterState.value.mode.isMediaOrMovies) moviesAsync.await() else emptyList()

                    val nextData = listOf(shows, movies)
                        .interleave()

                    itemsState.update { items ->
                        items
                            ?.plus(nextData)
                            ?.distinctBy { it.key }
                            ?.toImmutableList()
                    }

                    pages += 1
                    hasMoreData = nextData.isNotEmpty()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingMoreState.update { Done }
            }
        }
    }

    fun hideRecommendation(show: Show) {
        viewModelScope.launch {
            try {
                itemsState.update { items ->
                    items
                        ?.filterNot { it is ShowItem && it.id == show.ids.trakt }
                        ?.toImmutableList()
                }
                hideRecommendedShowUseCase.hideShow(show.ids.trakt)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
                events.emit(AllDiscoverEvent.HideError)
                loadData()
            }
        }
    }

    fun hideRecommendation(movie: Movie) {
        viewModelScope.launch {
            try {
                itemsState.update { items ->
                    items
                        ?.filterNot { it is MovieItem && it.id == movie.ids.trakt }
                        ?.toImmutableList()
                }
                hideRecommendedMovieUseCase.hideMovie(movie.ids.trakt)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
                events.emit(AllDiscoverEvent.HideError)
                loadData()
            }
        }
    }

    fun setFilter(filter: GlobalFilter) {
        filterState.update { filter }
        loadData()
    }

    val state = combine(
        typeState,
        filterState,
        collectionStateProvider.stateFlow,
        itemsState,
        loadingState,
        loadingMoreState,
        userState,
        errorState,
    ) { state ->
        AllDiscoverState(
            type = state[0] as DiscoverSection,
            filter = state[1] as GlobalFilter,
            collection = state[2] as UserCollectionState,
            items = state[3] as ImmutableList<DiscoverItem>?,
            loading = state[4] as LoadingState,
            loadingMore = state[5] as LoadingState,
            user = state[6] as? User,
            error = state[7] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )

    val events: Flow<AllDiscoverEvent>
        field = MutableSharedFlow<AllDiscoverEvent>(replay = 0)
}
