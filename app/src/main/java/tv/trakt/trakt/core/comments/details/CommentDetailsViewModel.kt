package tv.trakt.trakt.core.comments.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.comments.usecases.GetCommentRepliesUseCase
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User

internal class CommentDetailsViewModel(
    private val comment: Comment,
    private val sessionManager: SessionManager,
    private val getRepliseUseCase: GetCommentRepliesUseCase,
) : ViewModel() {
    private val initialState = CommentDetailsState()

    private val commentState = MutableStateFlow(comment)
    private val commentReplies = MutableStateFlow(initialState.replies)
    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }
                val replies = getRepliseUseCase.getCommentReplies(comment.id)
                commentReplies.value = replies
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.value = error
                    Timber.w(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<CommentDetailsState> = combine(
        commentState,
        commentReplies,
        userState,
        loadingState,
        errorState,
    ) { state ->
        CommentDetailsState(
            comment = state[0] as Comment?,
            replies = state[1] as ImmutableList<Comment>?,
            user = state[2] as User?,
            loading = state[3] as LoadingState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
