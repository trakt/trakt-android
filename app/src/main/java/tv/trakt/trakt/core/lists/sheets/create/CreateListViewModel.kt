package tv.trakt.trakt.core.lists.sheets.create

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
import tv.trakt.trakt.common.helpers.extensions.getHttpErrorCode
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.lists.sheets.create.usecases.CreateListUseCase

private const val HTTP_ERROR_CODE_LISTS_LIMIT = 420

internal class CreateListViewModel(
    private val createListUseCase: CreateListUseCase,
) : ViewModel() {
    private val initialState = CreateListState()

    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val limitErrorState = MutableStateFlow(initialState.listsLimitError)

    fun createList(
        name: String,
        description: String?,
    ) {
        if (loadingState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingState.update { LOADING }
                createListUseCase.createList(
                    name = name,
                    description = description,
                )
                loadingState.update { DONE }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (error.getHttpErrorCode() == HTTP_ERROR_CODE_LISTS_LIMIT) {
                        limitErrorState.update { error }
                    } else {
                        errorState.update { error }
                    }
                }
                loadingState.update { IDLE }
            }
        }
    }

    val state: StateFlow<CreateListState> = combine(
        loadingState,
        errorState,
        limitErrorState,
    ) { s1, s2, s3 ->
        CreateListState(
            loading = s1,
            error = s2,
            listsLimitError = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
