@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.discover.sections.anticipated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
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
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.usecases.GetDiscoverMediaUseCase
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class DiscoverAnticipatedViewModel(
    private val filterManager: GlobalFilterManager,
    private val getAnticipatedShowsUseCase: GetAnticipatedShowsUseCase,
    private val getAnticipatedMoviesUseCase: GetAnticipatedMoviesUseCase,
    private val getDiscoverMediaUseCase: GetDiscoverMediaUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = DiscoverAnticipatedState()

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
            Media -> getDiscoverMediaUseCase.getLocalMedia(DiscoverSection.Anticipated)
            Shows -> getAnticipatedShowsUseCase.getLocalShows()
            Movies -> getAnticipatedMoviesUseCase.getLocalMovies()
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
            Media -> getDiscoverMediaUseCase.getMedia(
                section = DiscoverSection.Anticipated,
                filters = filter,
            )
            Shows -> getAnticipatedShowsUseCase.getShows(filters = filter)
            Movies -> getAnticipatedMoviesUseCase.getMovies(filters = filter)
        }

        itemsState.update { items }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            val key = when (filterState.value.mode) {
                Media -> CollapsingKey.DISCOVER_MEDIA_ANTICIPATED
                Shows -> CollapsingKey.DISCOVER_SHOWS_ANTICIPATED
                Movies -> CollapsingKey.DISCOVER_MOVIES_ANTICIPATED
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
                Media -> CollapsingKey.DISCOVER_MEDIA_ANTICIPATED
                Shows -> CollapsingKey.DISCOVER_SHOWS_ANTICIPATED
                Movies -> CollapsingKey.DISCOVER_MOVIES_ANTICIPATED
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
        DiscoverAnticipatedState(
            items = state[0] as ImmutableList<DiscoverItem>?,
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
}
