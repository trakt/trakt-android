package tv.trakt.trakt.core.discover.sections.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.interleave
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.sections.all.navigation.DiscoverDestination
import tv.trakt.trakt.core.discover.sections.all.usecases.GetAllDiscoverMoviesUseCase
import tv.trakt.trakt.core.discover.sections.all.usecases.GetAllDiscoverShowsUseCase
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.user.CollectionStateProvider
import tv.trakt.trakt.core.user.UserCollectionState

@Suppress("UNCHECKED_CAST")
internal class AllDiscoverViewModel(
    savedStateHandle: SavedStateHandle,
    analytics: Analytics,
    private val modeManager: MediaModeManager,
    private val getShowsUseCase: GetAllDiscoverShowsUseCase,
    private val getMoviesUseCase: GetAllDiscoverMoviesUseCase,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<DiscoverDestination>()

    private val initialState = AllDiscoverState()
    private val initialMode = modeManager.getMode()

    private val modeState = MutableStateFlow(initialMode)
    private val filterState = MutableStateFlow(initialMode)
    private val typeState = MutableStateFlow(destination.source)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var currentPage: Int = 1

    init {
        loadData()

        observeMode()
        observeData()

        analytics.logScreenView(
            screenName = "all_discover_${destination.source.name.lowercase()}",
        )
    }

    private fun observeMode() {
        modeManager.observeMode()
            .onEach { value ->
                modeState.update { value }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun observeData() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadLocalData()

                coroutineScope {
                    val showsAsync = async { getShowsUseCase.getShows(destination.source) }
                    val moviesAsync = async { getMoviesUseCase.getMovies(destination.source) }

                    val shows = if (filterState.value.isMediaOrShows) showsAsync.await() else emptyList()
                    val movies = if (filterState.value.isMediaOrMovies) moviesAsync.await() else emptyList()

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
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun loadLocalData() {
        return coroutineScope {
            val localShowsAsync = async { getShowsUseCase.getLocalShows(destination.source) }
            val localMoviesAsync = async { getMoviesUseCase.getLocalMovies(destination.source) }

            val localShows = if (filterState.value.isMediaOrShows) localShowsAsync.await() else emptyList()
            val localMovies = if (filterState.value.isMediaOrMovies) localMoviesAsync.await() else emptyList()

            if (localShows.isNotEmpty() || localMovies.isNotEmpty()) {
                itemsState.update {
                    listOf(localShows, localMovies)
                        .interleave()
                        .toImmutableList()
                }
                loadingState.update { DONE }
            } else {
                loadingState.update { LOADING }
            }
        }
    }

    fun loadMoreData() {
        if (
            itemsState.value.isNullOrEmpty() ||
            loadingState.value.isLoading ||
            loadingMoreState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            try {
                loadingMoreState.update { LOADING }

                val showsAsync = async {
                    getShowsUseCase.getShows(
                        source = destination.source,
                        page = currentPage + 1,
                        skipLocal = true,
                    )
                }

                val moviesAsync = async {
                    getMoviesUseCase.getMovies(
                        source = destination.source,
                        page = currentPage + 1,
                        skipLocal = true,
                    )
                }

                val shows = if (filterState.value.isMediaOrShows) showsAsync.await() else emptyList()
                val movies = if (filterState.value.isMediaOrMovies) moviesAsync.await() else emptyList()

                val nextData = listOf(shows, movies)
                    .interleave()

                itemsState.update { items ->
                    items
                        ?.plus(nextData)
                        ?.distinctBy { it.key }
                        ?.toImmutableList()
                }

                currentPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingMoreState.update { DONE }
            }
        }
    }

    fun setFilter(filter: MediaMode) {
        filterState.update { filter }
        loadData()
    }

    val state = combine(
        modeState,
        typeState,
        filterState,
        collectionStateProvider.stateFlow,
        itemsState,
        loadingState,
        loadingMoreState,
        errorState,
    ) { state ->
        AllDiscoverState(
            mode = state[0] as MediaMode,
            type = state[1] as DiscoverSection,
            filter = state[2] as MediaMode,
            collection = state[3] as UserCollectionState,
            items = state[4] as ImmutableList<DiscoverItem>?,
            loading = state[5] as LoadingState,
            loadingMore = state[6] as LoadingState,
            error = state[7] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
