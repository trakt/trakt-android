package tv.trakt.trakt.core.discover.sections.trending.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
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
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.interleave
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_ALL_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingShowsUseCase
import tv.trakt.trakt.core.main.helpers.MediaModeProvider
import tv.trakt.trakt.core.main.model.MediaMode

@Suppress("UNCHECKED_CAST")
internal class AllDiscoverTrendingViewModel(
    private val modeProvider: MediaModeProvider,
    private val getTrendingShowsUseCase: GetTrendingShowsUseCase,
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = AllDiscoverTrendingState()

    private val modeState = MutableStateFlow(modeProvider.getMode())
    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var pages: Int = 1

    init {
        loadBackground()
        loadData()
        observeMode()

        analytics.logScreenView(
            screenName = "all_discover_trending",
        )
    }

    private fun observeMode() {
        modeProvider.observeMode()
            .onEach { value ->
                modeState.update { value }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadLocalData()

                coroutineScope {
                    val showsAsync = async {
                        getTrendingShowsUseCase.getShows(
                            page = 1,
                            limit = DEFAULT_ALL_LIMIT,
                        )
                    }
                    val moviesAsync = async {
                        getTrendingMoviesUseCase.getMovies(
                            page = 1,
                            limit = DEFAULT_ALL_LIMIT,
                        )
                    }

                    val shows = if (modeState.value.isMediaOrShows) showsAsync.await() else emptyList()
                    val movies = if (modeState.value.isMediaOrMovies) moviesAsync.await() else emptyList()

                    itemsState.update {
                        listOf(shows, movies)
                            .interleave()
                            .toImmutableList()
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun loadLocalData() {
        return coroutineScope {
            val localShowsAsync = async { getTrendingShowsUseCase.getLocalShows() }
            val localMoviesAsync = async { getTrendingMoviesUseCase.getLocalMovies() }

            val localShows = if (modeState.value.isMediaOrShows) localShowsAsync.await() else emptyList()
            val localMovies = if (modeState.value.isMediaOrMovies) localMoviesAsync.await() else emptyList()

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
                    getTrendingShowsUseCase.getShows(
                        page = pages + 1,
                        limit = DEFAULT_ALL_LIMIT,
                        skipLocal = true,
                    )
                }

                val moviesAsync = async {
                    getTrendingMoviesUseCase.getMovies(
                        page = pages + 1,
                        limit = DEFAULT_ALL_LIMIT,
                        skipLocal = true,
                    )
                }
                val shows = if (modeState.value.isMediaOrShows) showsAsync.await() else emptyList()
                val movies = if (modeState.value.isMediaOrMovies) moviesAsync.await() else emptyList()

                val nextData = listOf(shows, movies)
                    .interleave()

                itemsState.update {
                    it?.plus(nextData)
                        ?.distinctBy { item -> item.key }
                        ?.toImmutableList()
                }

                pages += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Failed to load more page data")
                }
            } finally {
                loadingMoreState.update { DONE }
            }
        }
    }

    val state = combine(
        modeState,
        backgroundState,
        itemsState,
        loadingState,
        loadingMoreState,
        errorState,
    ) { state ->
        AllDiscoverTrendingState(
            mode = state[0] as MediaMode,
            backgroundUrl = state[1] as String,
            items = state[2] as ImmutableList<DiscoverItem>?,
            loading = state[3] as LoadingState,
            loadingMore = state[4] as LoadingState,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
