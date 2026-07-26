package tv.trakt.trakt.app.core.lists.details.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.lists.ListsConfig.LISTS_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.filters.TvListFilterConfiguration
import tv.trakt.trakt.app.core.lists.filters.TvListRequest
import tv.trakt.trakt.app.core.lists.usecases.GetListsMoviesWatchlistUseCase
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting

internal class MoviesWatchlistViewModel(
    private val getListItemsUseCase: GetListsMoviesWatchlistUseCase,
) : ViewModel() {
    private val filterConfiguration = TvListFilterConfiguration.MoviesWatchlist
    private val _state = MutableStateFlow(MoviesWatchlistState())
    val state = _state.asStateFlow()

    private var requestJob: Job? = null
    private var nextDataPage = 1
    private var hasMoreData = true

    init {
        reload()
    }

    fun applyFilter(filter: GlobalFilter) {
        _state.update {
            it.copy(filter = filterConfiguration.normalize(filter))
        }
        reload()
    }

    fun applySorting(sorting: Sorting) {
        _state.update {
            it.copy(sorting = sorting)
        }
        reload()
    }

    private fun reload() {
        requestJob?.cancel()
        nextDataPage = 1
        hasMoreData = true
        _state.update {
            it.copy(
                isLoading = true,
                isLoadingPage = false,
                error = null,
            )
        }

        val request = _state.value.toRequest(page = 1)
        requestJob = viewModelScope.launch {
            try {
                val page = getListItemsUseCase.getMovies(request)
                nextDataPage = page.nextPage
                hasMoreData = page.hasMore
                _state.update {
                    it.copy(
                        isLoading = false,
                        movies = page.items,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error,
                        )
                    }
                }
            }
        }
    }

    fun loadNextDataPage() {
        val currentState = _state.value
        if (currentState.isLoading || currentState.isLoadingPage || !hasMoreData) return

        _state.update {
            it.copy(
                isLoadingPage = true,
                error = null,
            )
        }
        val request = _state.value.toRequest(page = nextDataPage)
        requestJob = viewModelScope.launch {
            try {
                val page = getListItemsUseCase.getMovies(request)
                nextDataPage = page.nextPage
                hasMoreData = page.hasMore
                _state.update { state ->
                    state.copy(
                        isLoadingPage = false,
                        movies = (
                            state.movies.orEmpty() + page.items
                        ).distinctBy { it.ids.trakt }
                            .toImmutableList(),
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    _state.update {
                        it.copy(
                            isLoadingPage = false,
                            error = error,
                        )
                    }
                }
            }
        }
    }

    private fun MoviesWatchlistState.toRequest(page: Int): TvListRequest {
        return TvListRequest(
            page = page,
            limit = LISTS_PAGE_LIMIT,
            filter = filter,
            sorting = sorting,
        )
    }
}
