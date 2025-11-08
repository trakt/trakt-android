package tv.trakt.trakt.core.discover.sections.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.model.DiscoverSection.ANTICIPATED
import tv.trakt.trakt.core.discover.model.DiscoverSection.POPULAR
import tv.trakt.trakt.core.discover.model.DiscoverSection.RECOMMENDED
import tv.trakt.trakt.core.discover.model.DiscoverSection.TRENDING
import tv.trakt.trakt.core.discover.sections.all.navigation.DiscoverDestination
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularMoviesUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingShowsUseCase
import tv.trakt.trakt.core.main.helpers.MediaModeProvider
import tv.trakt.trakt.core.main.model.MediaMode

@Suppress("UNCHECKED_CAST")
internal class AllDiscoverViewModel(
    savedStateHandle: SavedStateHandle,
    analytics: Analytics,
    private val modeProvider: MediaModeProvider,
    // Trending
    private val getTrendingShowsUseCase: GetTrendingShowsUseCase,
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    // Anticipated
    private val getAnticipatedShowsUseCase: GetAnticipatedShowsUseCase,
    private val getAnticipatedMoviesUseCase: GetAnticipatedMoviesUseCase,
    // Popular
    private val getPopularShowsUseCase: GetPopularShowsUseCase,
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    // Recommended
    private val getRecommendedShowsUseCase: GetRecommendedShowsUseCase,
    private val getRecommendedMoviesUseCase: GetRecommendedMoviesUseCase,
) : ViewModel() {
    private val initialState = AllDiscoverState()
    private val destination = savedStateHandle.toRoute<DiscoverDestination>()

    private val modeState = MutableStateFlow(modeProvider.getMode())
    private val typeState = MutableStateFlow(destination.source)

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
            screenName = "all_discover_${destination.source.name.lowercase()}",
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
                    val showsAsync = async { getShows() }
                    val moviesAsync = async { getMovies() }

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
            val localShowsAsync = async { getLocalShows() }
            val localMoviesAsync = async { getLocalMovies() }

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
                    getShows(
                        page = pages + 1,
                        skipLocal = true,
                    )
                }

                val moviesAsync = async {
                    getMovies(
                        page = pages + 1,
                        skipLocal = true,
                    )
                }

                val shows = if (modeState.value.isMediaOrShows) showsAsync.await() else emptyList()
                val movies = if (modeState.value.isMediaOrMovies) moviesAsync.await() else emptyList()

                val nextData = listOf(shows, movies)
                    .interleave()

                itemsState.update { items ->
                    items
                        ?.plus(nextData)
                        ?.distinctBy { it.key }
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

    private suspend fun getShows(
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<DiscoverItem> {
        return when (destination.source) {
            TRENDING -> getTrendingShowsUseCase.getShows(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
            )
            ANTICIPATED -> getAnticipatedShowsUseCase.getShows(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
            )
            POPULAR -> getPopularShowsUseCase.getShows(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
            )
            RECOMMENDED -> getRecommendedShowsUseCase.getShows(
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
            )
        }
    }

    private suspend fun getMovies(
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<DiscoverItem> {
        return when (destination.source) {
            TRENDING -> getTrendingMoviesUseCase.getMovies(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
            )
            ANTICIPATED -> getAnticipatedMoviesUseCase.getMovies(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
            )
            POPULAR -> getPopularMoviesUseCase.getMovies(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
            )
            RECOMMENDED -> getRecommendedMoviesUseCase.getMovies(
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
            )
        }
    }

    private suspend fun getLocalShows(): ImmutableList<DiscoverItem> {
        return when (destination.source) {
            TRENDING -> getTrendingShowsUseCase.getLocalShows()
            ANTICIPATED -> getAnticipatedShowsUseCase.getLocalShows()
            POPULAR -> getPopularShowsUseCase.getLocalShows()
            RECOMMENDED -> getRecommendedShowsUseCase.getLocalShows()
        }
    }

    private suspend fun getLocalMovies(): ImmutableList<DiscoverItem> {
        return when (destination.source) {
            TRENDING -> getTrendingMoviesUseCase.getLocalMovies()
            ANTICIPATED -> getAnticipatedMoviesUseCase.getLocalMovies()
            POPULAR -> getPopularMoviesUseCase.getLocalMovies()
            RECOMMENDED -> getRecommendedMoviesUseCase.getLocalMovies()
        }
    }

    val state = combine(
        modeState,
        typeState,
        backgroundState,
        itemsState,
        loadingState,
        loadingMoreState,
        errorState,
    ) { state ->
        AllDiscoverState(
            mode = state[0] as MediaMode,
            type = state[1] as DiscoverSection,
            backgroundUrl = state[2] as String,
            items = state[3] as ImmutableList<DiscoverItem>?,
            loading = state[4] as LoadingState,
            loadingMore = state[5] as LoadingState,
            error = state[6] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
