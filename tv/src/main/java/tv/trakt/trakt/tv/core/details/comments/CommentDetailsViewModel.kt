package tv.trakt.trakt.tv.core.details.comments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.tv.common.model.Comment
import tv.trakt.trakt.tv.core.details.comments.usecases.GetCommentRepliesUseCase

internal class CommentDetailsViewModel(
    private val getCommentRepliesUseCase: GetCommentRepliesUseCase,
) : ViewModel() {
    private val initialState = CommentDetailsState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val commentRepliesState = MutableStateFlow(initialState.commentReplies)

    private var loadingJob: Job? = null

    fun loadCommentReplies(commentId: Int) {
        loadingJob = viewModelScope.launch {
            delay(500)
            loadingState.update { true }
        }
        viewModelScope.launch {
            try {
                commentRepliesState.update { null }
                val replies = getCommentRepliesUseCase.getCommentReplies(commentId)
                commentRepliesState.update { replies }
            } catch (e: Exception) {
                e.rethrowCancellation {
                    Log.e("CommentDetailsViewModel", "Error loading comment replies", e)
                }
            } finally {
                loadingJob?.cancel()
                loadingState.update { false }
            }
        }
    }

    val state: StateFlow<CommentDetailsState> = combine(
        loadingState,
        commentRepliesState,
    ) { states ->
        @Suppress("UNCHECKED_CAST")
        CommentDetailsState(
            isLoading = states[0] as Boolean,
            commentReplies = states[1] as ImmutableList<Comment>?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
