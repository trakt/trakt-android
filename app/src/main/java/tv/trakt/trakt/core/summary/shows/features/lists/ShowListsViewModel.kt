package tv.trakt.trakt.core.summary.shows.features.lists

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
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.shows.features.lists.usecases.GetShowListsUseCase

internal class ShowListsViewModel(
    private val show: Show,
    private val getListsUseCase: GetShowListsUseCase,
) : ViewModel() {
    private val initialState = ShowListsState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                coroutineScope {
                    val showId = show.ids.trakt
                    val officialListsAsync = async { getListsUseCase.getOfficialLists(showId) }
                    val personalListsAsync = async { getListsUseCase.getPersonalLists(showId) }

                    itemsState.update {
                        (officialListsAsync.await() + personalListsAsync.await())
                            .take(1)
                            .toImmutableList()
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ShowListsState> = combine(
        itemsState,
        loadingState,
        errorState,
    ) { state ->
        ShowListsState(
            items = state[0] as ImmutableList<CustomList>?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
