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
import tv.trakt.trakt.app.core.home.sections.recommended.model.RecommendedItem
import tv.trakt.trakt.app.core.movies.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show

private const val ALL_ITEMS_LIMIT = 100

internal class RecommendedViewAllViewModel(
    private val getRecommendedShowsUseCase: GetRecommendedShowsUseCase,
    private val getRecommendedMoviesUseCase: GetRecommendedMoviesUseCase,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val initialState = RecommendedViewAllState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        observeData()
    }

    private fun observeData() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }

                coroutineScope {
                    val showsAsync = async {
                        getRecommendedShowsUseCase.getRecommendedShows(limit = ALL_ITEMS_LIMIT, page = 1)
                    }
                    val moviesAsync = async {
                        getRecommendedMoviesUseCase.getRecommendedMovies(limit = ALL_ITEMS_LIMIT, page = 1)
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
    ) { loading, items, collection, error ->
        RecommendedViewAllState(
            isLoading = loading,
            items = items,
            collection = collection,
            error = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
