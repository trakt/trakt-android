package tv.trakt.trakt.tv.core.details.lists.details.shows

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.core.details.lists.details.CustomListDetailsConfig.CUSTOM_LIST_PAGE_LIMIT
import tv.trakt.trakt.tv.core.details.lists.details.shows.navigation.CustomListShowsDestination
import tv.trakt.trakt.tv.core.details.lists.details.shows.usecases.GetListItemsUseCase

internal class CustomListShowsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getListItemsUseCase: GetListItemsUseCase,
) : ViewModel() {
    private val initialState = CustomListShowsState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val showsState = MutableStateFlow(initialState.shows)
    private val errorState = MutableStateFlow(initialState.error)

    val destination = savedStateHandle.toRoute<CustomListShowsDestination>()

    private var nextDataPage: Int = 1
    private var hasMoreData: Boolean = true

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }
                val shows = getListItemsUseCase.getListItems(
                    listId = TraktId(destination.listId),
                    page = nextDataPage,
                )
                showsState.update { shows }
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Log.e("CustomListShowsViewModel", "Error loading shows for list: ${destination.listId} $error")
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

                val shows = getListItemsUseCase.getListItems(
                    listId = TraktId(destination.listId),
                    page = nextDataPage,
                )

                showsState.update {
                    it?.toPersistentList()?.plus(shows)
                }

                hasMoreData = (shows.size >= CUSTOM_LIST_PAGE_LIMIT)
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Log.e(
                        "CustomListShowsViewModel",
                        "Error loading next page of shows for list: ${destination.listId} $error",
                    )
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
        CustomListShowsState(
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
