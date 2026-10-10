@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.home.sections.recommended

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode.Media
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedItem
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedItem.MovieItem
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedItem.ShowItem
import tv.trakt.trakt.core.home.sections.recommended.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.HideRecommendedMovieUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.HideRecommendedShowUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.media.GetRecommendedMediaUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class HomeRecommendedViewModel(
    private val filterManager: GlobalFilterManager,
    private val getRecommendedShowsUseCase: GetRecommendedShowsUseCase,
    private val getRecommendedMoviesUseCase: GetRecommendedMoviesUseCase,
    private val getRecommendedMediaUseCase: GetRecommendedMediaUseCase,
    private val hideRecommendedShowUseCase: HideRecommendedShowUseCase,
    private val hideRecommendedMovieUseCase: HideRecommendedMovieUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = HomeRecommendedState()

    private val filterState = MutableStateFlow(filterManager.getFilter())
    private val collapseState = MutableStateFlow(isCollapsed())
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var collapseJob: Job? = null

    init {
        loadData()
        observeMode()
    }

    private fun observeMode() {
        filterManager.observeFilter()
            .onEach { value ->
                filterState.update { value }
                collapseState.update { isCollapsed() }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadLocalData()
                loadRemoteData()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                dataJob = null
            }
        }
    }

    private suspend fun loadLocalData() {
        val localItems = when (filterState.value.mode) {
            Media -> getRecommendedMediaUseCase.getLocalMedia()
            Shows -> getRecommendedShowsUseCase.getLocalShows()
            Movies -> getRecommendedMoviesUseCase.getLocalMovies()
        }

        if (localItems.isNotEmpty()) {
            itemsState.update { localItems }
            loadingState.update { Done }
        } else {
            loadingState.update { Loading }
        }
    }

    private suspend fun loadRemoteData() {
        val filter = filterState.value
        val items = when (filter.mode) {
            Media -> getRecommendedMediaUseCase.getMedia(filters = filter)
            Shows -> getRecommendedShowsUseCase.getShows(filters = filter)
            Movies -> getRecommendedMoviesUseCase.getMovies(filters = filter)
        }

        itemsState.update { items }
    }

    fun hideRecommendation(show: Show) {
        viewModelScope.launch {
            try {
                itemsState.update { items ->
                    items
                        ?.filterNot { it is ShowItem && it.id == show.ids.trakt }
                        ?.toImmutableList()
                }
                hideRecommendedShowUseCase.hideShow(show.ids.trakt)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
                events.emit(HomeRecommendedEvent.HideError)
                loadData()
            }
        }
    }

    fun hideRecommendation(movie: Movie) {
        viewModelScope.launch {
            try {
                itemsState.update { items ->
                    items
                        ?.filterNot { it is MovieItem && it.id == movie.ids.trakt }
                        ?.toImmutableList()
                }
                hideRecommendedMovieUseCase.hideMovie(movie.ids.trakt)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
                events.emit(HomeRecommendedEvent.HideError)
                loadData()
            }
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            val key = when (filterState.value.mode) {
                Media -> CollapsingKey.DISCOVER_MEDIA_RECOMMENDED
                Shows -> CollapsingKey.DISCOVER_SHOWS_RECOMMENDED
                Movies -> CollapsingKey.DISCOVER_MOVIES_RECOMMENDED
            }
            when {
                collapsed -> collapsingManager.collapse(key)
                else -> collapsingManager.expand(key)
            }
        }
    }

    private fun isCollapsed(): Boolean {
        return collapsingManager.isCollapsed(
            key = when (filterState.value.mode) {
                Media -> CollapsingKey.DISCOVER_MEDIA_RECOMMENDED
                Shows -> CollapsingKey.DISCOVER_SHOWS_RECOMMENDED
                Movies -> CollapsingKey.DISCOVER_MOVIES_RECOMMENDED
            },
        )
    }

    val state = combine(
        itemsState,
        filterState,
        collapseState,
        loadingState,
        errorState,
    ) { state ->
        HomeRecommendedState(
            items = state[0] as ImmutableList<RecommendedItem>?,
            filter = state[1] as GlobalFilter?,
            collapsed = state[2] as Boolean,
            loading = state[3] as LoadingState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )

    val events: Flow<HomeRecommendedEvent>
        field = MutableSharedFlow<HomeRecommendedEvent>(replay = 0)
}
