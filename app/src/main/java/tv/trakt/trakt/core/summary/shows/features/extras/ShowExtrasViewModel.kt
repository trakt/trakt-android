package tv.trakt.trakt.core.summary.shows.features.extras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
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
import tv.trakt.trakt.common.model.ExtraVideo
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.shows.features.extras.usecases.GetShowExtrasUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class ShowExtrasViewModel(
    private val show: Show,
    private val getExtrasUseCase: GetShowExtrasUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = ShowExtrasState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val filterItemsState = MutableStateFlow(initialState.items)
    private val filtersState = MutableStateFlow(initialState.filters)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val collapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.SHOW_EXTRAS))

    private var collapseJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                val items = getExtrasUseCase.getExtraVideos(show.ids.trakt)
                itemsState.update { items }
                filterItemsState.update { items }

                loadFilters(items)
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

    private fun loadFilters(items: List<ExtraVideo>) {
        val filters = items
            .map { it.type }
            .distinct()
            .sortedWith(
                compareByDescending<String> { it == "trailer" }
                    .thenBy { it.length },
            )
            .toImmutableList()

        filtersState.update {
            ShowExtrasState.FiltersState(
                filters = filters,
                selectedFilter = null,
            )
        }
    }

    fun toggleFilter(filter: String) {
        val current = filtersState.value
        val newFilter = when (current.selectedFilter) {
            filter -> null
            else -> filter
        }

        filtersState.update {
            current.copy(selectedFilter = newFilter)
        }

        val allItems = itemsState.value ?: return
        val newItems = when (newFilter) {
            null -> allItems
            else -> allItems.filter { it.type == newFilter }
        }

        filterItemsState.update {
            newItems.toImmutableList()
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }
        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.SHOW_EXTRAS)
                else -> collapsingManager.expand(CollapsingKey.SHOW_EXTRAS)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ShowExtrasState> = combine(
        filterItemsState,
        filtersState,
        loadingState,
        errorState,
        collapseState,
    ) { state ->
        ShowExtrasState(
            items = state[0] as ImmutableList<ExtraVideo>?,
            filters = state[1] as ShowExtrasState.FiltersState,
            loading = state[2] as LoadingState,
            error = state[3] as Exception?,
            collapsed = state[4] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
