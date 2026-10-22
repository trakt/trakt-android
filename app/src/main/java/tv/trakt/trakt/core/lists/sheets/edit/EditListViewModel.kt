package tv.trakt.trakt.core.lists.sheets.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.data.local.UserListsLocalSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.sheets.edit.usecases.EditListUseCase

internal class EditListViewModel(
    private val editListUseCase: EditListUseCase,
    private val userListsLocalSource: UserListsLocalSource,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = EditListState()

    private val loadingEditState = MutableStateFlow(initialState.loadingEdit)
    private val loadingDeleteState = MutableStateFlow(initialState.loadingDelete)
    private val errorState = MutableStateFlow(initialState.error)

    fun editList(
        id: TraktId,
        name: String,
        description: String?,
        privacy: CustomList.Privacy,
        sorting: Sorting?,
    ) {
        if (loadingEditState.value.isLoading || loadingDeleteState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingEditState.update { Loading }

                editListUseCase.editList(
                    listId = id,
                    name = name,
                    description = description,
                    privacy = privacy,
                    sorting = sorting,
                )
                analytics.lists.logListEdit()

                loadingEditState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
                loadingEditState.update { Idle }
            }
        }
    }

    fun deleteList(id: TraktId) {
        if (loadingEditState.value.isLoading || loadingDeleteState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingDeleteState.value = Loading

                editListUseCase.deleteList(
                    listId = id,
                )
                analytics.lists.logListDelete()

                // Clear cached lists to force refresh next time lists are accessed.
                userListsLocalSource.clear()

                loadingDeleteState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
                loadingDeleteState.update { Idle }
            }
        }
    }

    val state: StateFlow<EditListState> = combine(
        loadingEditState,
        loadingDeleteState,
        errorState,
    ) { s1, s2, s3 ->
        EditListState(
            loadingEdit = s1,
            loadingDelete = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
