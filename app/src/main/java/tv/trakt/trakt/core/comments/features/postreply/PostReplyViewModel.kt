package tv.trakt.trakt.core.comments.features.postreply

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.comments.usecases.PostReplyUseCase
import kotlin.time.Duration.Companion.seconds

internal class PostReplyViewModel(
    private val comment: Comment,
    private val sessionManager: SessionManager,
    private val postReplyUseCase: PostReplyUseCase,
) : ViewModel() {
    private val initialState = PostReplyState()

    private val loadingState = MutableStateFlow(initialState.loading)
    private val userState = MutableStateFlow(initialState.user)
    private val commentUserState = MutableStateFlow(comment.user)
    private val resultState = MutableStateFlow(initialState.result)
    private val errorState = MutableStateFlow(initialState.error)

    private var job: Job? = null

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun submitReply(
        reply: String,
        spoiler: Boolean,
    ) {
        if (job?.isActive == true) {
            return
        }
        job = viewModelScope.launch {
            try {
                loadingState.update { LOADING }
                delay(1.seconds)
                resultState.update {
                    postReplyUseCase.postReply(
                        commentId = comment.id.toTraktId(),
                        text = reply,
                        spoiler = spoiler,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    loadingState.update { DONE }
                    Timber.recordError(error)
                }
            } finally {
                job = null
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
        commentUserState,
        resultState,
        errorState,
    ) { state ->
        PostReplyState(
            loading = state[0] as LoadingState,
            user = state[1] as User?,
            commentUser = state[2] as User?,
            result = state[3] as Comment?,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
