package tv.trakt.trakt.app.core.lists.details.shows

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
import tv.trakt.trakt.app.core.lists.ListsConfig.LISTS_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.usecases.GetListsShowsWatchlistUseCase
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation

internal class ShowsWatchlistViewModel(
    private val getListItemsUseCase: GetListsShowsWatchlistUseCase,
) : ViewModel() {
    private val initialState = ShowsWatchlistState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val showsState = MutableStateFlow(initialState.shows)
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
                val shows = getListItemsUseCase.getShows(LISTS_PAGE_LIMIT)
                showsState.update { shows }
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

                val shows = getListItemsUseCase.getShows(
                    limit = LISTS_PAGE_LIMIT,
                    page = nextDataPage,
                )

                showsState.update {
                    it?.toPersistentList()?.plus(shows)
                }

                hasMoreData = (shows.size >= LISTS_PAGE_LIMIT)
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
        showsState,
        errorState,
    ) { s1, s2, s3, s4 ->
        ShowsWatchlistState(
            isLoading = s1,
            isLoadingPage = s2,
            shows = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
