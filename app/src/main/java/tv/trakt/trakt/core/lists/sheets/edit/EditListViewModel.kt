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
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sheets.edit.usecases.EditListUseCase

internal class EditListViewModel(
    private val editListUseCase: EditListUseCase,
) : ViewModel() {
    private val initialState = EditListState()

    private val loadingEditState = MutableStateFlow(initialState.loadingEdit)
    private val loadingDeleteState = MutableStateFlow(initialState.loadingDelete)
    private val errorState = MutableStateFlow(initialState.error)

    fun editList(
        id: TraktId,
        name: String,
        description: String?,
    ) {
        if (loadingEditState.value.isLoading || loadingDeleteState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingEditState.value = LOADING

                editListUseCase.editList(
                    listId = id,
                    name = name,
                    description = description,
                )

                loadingEditState.update { DONE }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
                loadingEditState.update { IDLE }
            }
        }
    }

    fun deleteList(id: TraktId) {
        if (loadingEditState.value.isLoading || loadingDeleteState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingDeleteState.value = LOADING

                editListUseCase.deleteList(
                    listId = id,
                )

                loadingDeleteState.update { DONE }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
                loadingDeleteState.update { IDLE }
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
