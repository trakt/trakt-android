package tv.trakt.trakt.core.comments.features.deletecomment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.comments.usecases.DeleteCommentUseCase

internal class DeleteCommentViewModel(
    private val commentId: TraktId,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = DeleteCommentState()

    private val loadingState = MutableStateFlow(initialState.loading)
    private val userState = MutableStateFlow(initialState.user)
    private val deletedState = MutableStateFlow(initialState.deleted)
    private val errorState = MutableStateFlow(initialState.error)

    private var job: Job? = null

    fun deleteComment() {
        if (job?.isActive == true) return

        loadingState.update { LoadingState.LOADING }
        errorState.update { null }

        job = viewModelScope.launch {
            try {
                deleteCommentUseCase.deleteComment(commentId)
                analytics.comments.logCommentRemove()
                deletedState.update { true }
            } catch (error: Exception) {
                loadingState.update { LoadingState.DONE }
                errorState.update { error }
            }
        }
    }

    fun clearError() {
        errorState.update { null }
    }

    override fun onCleared() {
        job?.cancel()
        job = null
        super.onCleared()
    }

    val state = combine(
        loadingState,
        userState,
        deletedState,
        errorState,
    ) { state ->
        DeleteCommentState(
            loading = state[0] as LoadingState,
            user = state[1] as User?,
            deleted = state[2] as Boolean,
            error = state[3] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
