package tv.trakt.trakt.app.core.movies.features.popular

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.movies.MoviesConfig.MOVIES_PAGE_LIMIT
import tv.trakt.trakt.app.core.movies.usecase.GetPopularMoviesUseCase
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation

internal class MoviesPopularViewAllViewModel(
    private val getItemsUseCase: GetPopularMoviesUseCase,
) : ViewModel() {
    private val initialState = MoviesPopularViewAllState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val moviesState = MutableStateFlow(initialState.movies)
    private val errorState = MutableStateFlow(initialState.error)

    private var nextDataPage: Int = 1
    private var hasMoreData: Boolean = true

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }

                val movies = getItemsUseCase.getPopularMovies(
                    limit = MOVIES_PAGE_LIMIT,
                    page = 1,
                )
                moviesState.update { movies }

                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun loadNextDataPage() {
        if (loadingPageState.value || !hasMoreData) {
            return
        }
        viewModelScope.launch {
            try {
                loadingPageState.update { true }

                val movies = getItemsUseCase.getPopularMovies(
                    limit = 50,
                    page = nextDataPage,
                )

                moviesState.update {
                    it?.toPersistentList()?.plus(movies)
                }

                hasMoreData = (movies.size >= 50)
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingPageState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        loadingPageState,
        moviesState,
        errorState,
    ) { s1, s2, s3, s4 ->
        MoviesPopularViewAllState(
            isLoading = s1,
            isLoadingPage = s2,
            movies = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
