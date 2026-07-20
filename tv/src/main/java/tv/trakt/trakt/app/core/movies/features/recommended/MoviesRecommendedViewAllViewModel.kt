package tv.trakt.trakt.app.core.movies.features.recommended

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.movies.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation

private const val ALL_ITEMS_LIMIT = 100

internal class MoviesRecommendedViewAllViewModel(
    private val getItemsUseCase: GetRecommendedMoviesUseCase,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val initialState = MoviesRecommendedViewAllState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val moviesState = MutableStateFlow(initialState.movies)
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

                val movies = getItemsUseCase.getRecommendedMovies(
                    limit = ALL_ITEMS_LIMIT,
                    page = 1,
                )
                moviesState.update { movies }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        moviesState,
        collectionStateProvider.stateFlow,
        errorState,
    ) { s1, s2, s3, s4 ->
        MoviesRecommendedViewAllState(
            isLoading = s1,
            movies = s2,
            collection = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
