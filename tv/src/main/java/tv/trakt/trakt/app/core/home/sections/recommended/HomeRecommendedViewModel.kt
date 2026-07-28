package tv.trakt.trakt.app.core.home.sections.recommended

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.Config.REFRESH_DATA_THRESHOLD_MINUTES
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.app.core.home.sections.recommended.model.RecommendedItem
import tv.trakt.trakt.app.core.movies.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State.FOREGROUND
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import java.time.ZonedDateTime

internal class HomeRecommendedViewModel(
    private val getRecommendedShowsUseCase: GetRecommendedShowsUseCase,
    private val getRecommendedMoviesUseCase: GetRecommendedMoviesUseCase,
    private val sessionManager: SessionManager,
    private val collectionStateProvider: CollectionStateProvider,
    private val appLifecycleProvider: AppLifecycleProvider,
) : ViewModel() {
    private val initialState = HomeRecommendedState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadedAt: ZonedDateTime? = null
    private var dataJob: Job? = null

    init {
        loadData()
        observeApp()
        observeData()
    }

    private fun observeData() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun observeApp() {
        appLifecycleProvider.observeState(FOREGROUND)
            .filter {
                loadedAt != null &&
                    nowUtc().minusMinutes(REFRESH_DATA_THRESHOLD_MINUTES).isAfter(loadedAt)
            }
            .onEach {
                loadData(showLoading = false)
            }
            .launchIn(viewModelScope)
    }

    private fun loadData(showLoading: Boolean = true) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    itemsState.update { EmptyImmutableList }
                    return@launch
                }

                if (showLoading) {
                    loadingState.update { true }
                }

                coroutineScope {
                    val showsAsync =
                        async { getRecommendedShowsUseCase.getRecommendedShows(limit = HOME_SECTION_LIMIT) }
                    val moviesAsync =
                        async { getRecommendedMoviesUseCase.getRecommendedMovies(limit = HOME_SECTION_LIMIT) }

                    val shows = showsAsync.await()
                    val movies = moviesAsync.await()

                    itemsState.update {
                        interleaveRecommended(shows, movies)
                            .take(HOME_SECTION_LIMIT)
                            .toImmutableList()
                    }
                }

                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error, "Error loading recommended items")
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    private fun interleaveRecommended(
        shows: List<Show>,
        movies: List<Movie>,
    ): List<RecommendedItem> =
        (0 until maxOf(shows.size, movies.size)).flatMap { index ->
            listOfNotNull(
                shows.getOrNull(index)?.let(RecommendedItem::ShowItem),
                movies.getOrNull(index)?.let(RecommendedItem::MovieItem),
            )
        }

    val state = combine(
        itemsState,
        loadingState,
        collectionStateProvider.stateFlow,
        errorState,
    ) { items, loading, collection, error ->
        HomeRecommendedState(
            items = items,
            isLoading = loading,
            collection = collection,
            error = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
