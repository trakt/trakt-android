package tv.trakt.trakt.core.summary.movies.features.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.summary.movies.features.lists.usecases.GetMovieListsUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class MovieListsViewModel(
    private val movie: Movie,
    private val getListsUseCase: GetMovieListsUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = MovieListsState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val collapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.MOVIE_LISTS))
    private val errorState = MutableStateFlow(initialState.error)

    private var collapseJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                coroutineScope {
                    val movieId = movie.ids.trakt
                    val officialListsAsync = async { getListsUseCase.getOfficialLists(movieId) }
                    val personalListsAsync = async { getListsUseCase.getPersonalLists(movieId) }

                    itemsState.update {
                        (officialListsAsync.await() + personalListsAsync.await())
                            .take(1)
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

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.MOVIE_LISTS)
                else -> collapsingManager.expand(CollapsingKey.MOVIE_LISTS)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<MovieListsState> = combine(
        itemsState,
        loadingState,
        collapseState,
        errorState,
    ) { state ->
        MovieListsState(
            items = state[0] as ImmutableList<CustomList>?,
            loading = state[1] as LoadingState,
            collapsed = state[2] as Boolean,
            error = state[3] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
