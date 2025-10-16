package tv.trakt.trakt.core.summary.movies.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates
import tv.trakt.trakt.core.summary.movies.features.history.usecases.GetMovieHistoryUseCase

@OptIn(FlowPreview::class)
internal class MovieHistoryViewModel(
    private val movie: Movie,
    private val getHistoryUseCase: GetMovieHistoryUseCase,
    private val movieDetailsUpdates: MovieDetailsUpdates,
) : ViewModel() {
    private val initialState = MovieHistoryState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        observeData()
    }

    private fun observeData() {
        movieDetailsUpdates.observeUpdates()
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(ignoreErrors = true)
            }
            .launchIn(viewModelScope)
    }

    fun loadData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            try {
                if (itemsState.value == initialState.items) {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    getHistoryUseCase.getHistory(movie.ids.trakt)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.w(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<MovieHistoryState> = combine(
        itemsState,
        loadingState,
        errorState,
    ) { state ->
        MovieHistoryState(
            items = state[0] as ImmutableList<HomeActivityItem.MovieItem>?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
