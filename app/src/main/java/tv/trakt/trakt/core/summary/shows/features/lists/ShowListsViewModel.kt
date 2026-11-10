package tv.trakt.trakt.core.summary.shows.features.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserLikedListsUseCase
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.summary.shows.features.lists.usecases.GetShowListsUseCase
import tv.trakt.trakt.helpers.collapsing.CollapseDelegate
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class ShowListsViewModel(
    private val show: Show,
    private val getListsUseCase: GetShowListsUseCase,
    private val loadUserLikedListsUseCase: LoadUserLikedListsUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = ShowListsState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val likedItemsState = MutableStateFlow(initialState.likedItems)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val collapse = CollapseDelegate(
        manager = collapsingManager,
        scope = viewModelScope,
        key = CollapsingKey.SHOW_LISTS,
    )

    init {
        loadData()
        loadUserLikedLists()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                coroutineScope {
                    val showId = show.ids.trakt
                    val officialListsAsync = async { getListsUseCase.getOfficialLists(showId) }
                    val personalListsAsync = async { getListsUseCase.getPersonalLists(showId) }

                    itemsState.update {
                        (officialListsAsync.await() + personalListsAsync.await())
                            .toImmutableList()
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private fun loadUserLikedLists() {
        viewModelScope.launch {
            try {
                likedItemsState.update {
                    loadUserLikedListsUseCase.loadIfNeeded().keys
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapse.set(collapsed)
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        itemsState,
        likedItemsState,
        loadingState,
        errorState,
        collapse.state,
    ) { state ->
        ShowListsState(
            items = state[0] as ImmutableList<CustomList>?,
            likedItems = state[1] as ImmutableSet<TraktId>?,
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
