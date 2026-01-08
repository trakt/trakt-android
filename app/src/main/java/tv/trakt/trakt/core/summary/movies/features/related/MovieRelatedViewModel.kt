package tv.trakt.trakt.core.summary.movies.features.related

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.summary.movies.features.related.usecases.GetMovieRelatedUseCase
import tv.trakt.trakt.core.user.CollectionStateProvider
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class MovieRelatedViewModel(
    private val movie: Movie,
    private val getRelatedMoviesUseCase: GetMovieRelatedUseCase,
    private val collectionStateProvider: CollectionStateProvider,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = MovieRelatedState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val collapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.MOVIE_RELATED))
    private val errorState = MutableStateFlow(initialState.error)

    private var collapseJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
        observeCollection()
    }

    private fun observeCollection() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                itemsState.update {
                    getRelatedMoviesUseCase.getRelatedMovies(movie.ids.trakt)
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
                collapsed -> collapsingManager.collapse(CollapsingKey.MOVIE_RELATED)
                else -> collapsingManager.expand(CollapsingKey.MOVIE_RELATED)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        itemsState,
        collectionStateProvider.stateFlow,
        loadingState,
        collapseState,
        errorState,
    ) { state ->
        MovieRelatedState(
            items = state[0] as ImmutableList<Movie>?,
            collection = state[1] as UserCollectionState,
            loading = state[2] as LoadingState,
            collapsed = state[3] as Boolean,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
