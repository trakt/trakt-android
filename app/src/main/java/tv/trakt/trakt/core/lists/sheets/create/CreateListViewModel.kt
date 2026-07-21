package tv.trakt.trakt.core.lists.sheets.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.data.local.UserListsLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_TRAKT_VIP_LIMIT
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.CustomList.Privacy.Private
import tv.trakt.trakt.common.model.CustomList.Privacy.Public
import tv.trakt.trakt.core.lists.sheets.create.usecases.CreateListUseCase

internal class CreateListViewModel(
    private val createListUseCase: CreateListUseCase,
    private val userListsLocalDataSource: UserListsLocalDataSource,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = CreateListState()

    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val limitErrorState = MutableStateFlow(initialState.listsLimitError)
    private val initialPrivacyState = MutableStateFlow(initialState.initialPrivacy)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val isPrivate = sessionManager.getProfile()?.isPrivate ?: return@launch
            initialPrivacyState.update {
                if (isPrivate) Private else Public
            }
        }
    }

    fun createList(
        name: String,
        description: String?,
        privacy: CustomList.Privacy,
    ) {
        if (loadingState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingState.update { Loading }
                createListUseCase.createList(
                    name = name,
                    description = description,
                    privacy = privacy,
                )

                // Clear cached lists to force refresh next time lists are accessed.
                userListsLocalDataSource.clear()

                loadingState.update { Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (error.getHttpCode() == HTTP_ERROR_TRAKT_VIP_LIMIT) {
                        limitErrorState.update { error }
                    } else {
                        errorState.update { error }
                    }
                }
                loadingState.update { Idle }
            }
        }
    }

    val state = combine(
        loadingState,
        errorState,
        limitErrorState,
        initialPrivacyState,
    ) { s1, s2, s3, s4 ->
        CreateListState(
            loading = s1,
            error = s2,
            listsLimitError = s3,
            initialPrivacy = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
