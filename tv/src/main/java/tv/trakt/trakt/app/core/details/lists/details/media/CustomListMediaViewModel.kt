package tv.trakt.trakt.app.core.details.lists.details.media

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
import timber.log.Timber
import tv.trakt.trakt.app.core.details.lists.details.CustomListDetailsConfig.CUSTOM_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.details.lists.details.media.navigation.CustomListMediaDestination
import tv.trakt.trakt.app.core.details.lists.details.media.usecases.GetListItemsUseCase
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId

internal class CustomListMediaViewModel(
    savedStateHandle: SavedStateHandle,
    private val getListItemsUseCase: GetListItemsUseCase,
) : ViewModel() {
    private val initialState = CustomListMediaState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)

    val destination = savedStateHandle.toRoute<CustomListMediaDestination>()

    private var nextDataPage: Int = 1
    private var hasMoreData: Boolean = true

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }
                val items = getListItemsUseCase.getListItems(
                    listId = destination.listId.toTraktId(),
                    page = nextDataPage,
                )
                itemsState.update { items }
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Error loading media for list: ${destination.listId} $error")
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

                val items = getListItemsUseCase.getListItems(
                    listId = TraktId(destination.listId),
                    page = nextDataPage,
                )

                itemsState.update {
                    it?.toPersistentList()?.plus(items)
                }

                hasMoreData = (items.size >= CUSTOM_LIST_PAGE_LIMIT)
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e("Error loading next page of media for list: ${destination.listId} $error")
                }
            } finally {
                loadingPageState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        loadingPageState,
        itemsState,
        errorState,
    ) { s1, s2, s3, s4 ->
        CustomListMediaState(
            isLoading = s1,
            isLoadingPage = s2,
            items = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
