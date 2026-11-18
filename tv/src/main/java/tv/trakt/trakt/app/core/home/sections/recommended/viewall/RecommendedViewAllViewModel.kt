package tv.trakt.trakt.app.core.home.sections.recommended.viewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.movies.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.data.local.MediaModeLocalSource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.ifMovies
import tv.trakt.trakt.common.model.ifShows
import tv.trakt.trakt.common.model.recommended.RecommendedItem

private const val ALL_ITEMS_LIMIT = 100

internal class RecommendedViewAllViewModel(
    private val getRecommendedShowsUseCase: GetRecommendedShowsUseCase,
    private val getRecommendedMoviesUseCase: GetRecommendedMoviesUseCase,
    private val collectionStateProvider: CollectionStateProvider,
    private val mediaModeLocalSource: MediaModeLocalSource,
) : ViewModel() {
    private val initialState = RecommendedViewAllState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)
    private val modeState = MutableStateFlow(initialState.mode)

    init {
        loadInitialData()
        observeData()
    }

    private fun observeData() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            modeState.update { mediaModeLocalSource.getMode() }
            loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }

                coroutineScope {
                    val showsAsync = async {
                        modeState.value.ifShows {
                            getRecommendedShowsUseCase.getRecommendedShows(limit = ALL_ITEMS_LIMIT, page = 1)
                        }
                    }
                    val moviesAsync = async {
                        modeState.value.ifMovies {
                            getRecommendedMoviesUseCase.getRecommendedMovies(limit = ALL_ITEMS_LIMIT, page = 1)
                        }
                    }

                    val shows = showsAsync.await()
                    val movies = moviesAsync.await()

                    itemsState.update {
                        interleaveRecommended(shows, movies).toImmutableList()
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
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
        loadingState,
        itemsState,
        collectionStateProvider.stateFlow,
        errorState,
        modeState,
    ) { loading, items, collection, error, mode ->
        RecommendedViewAllState(
            isLoading = loading,
            items = items,
            collection = collection,
            error = error,
            mode = mode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
