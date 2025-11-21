package tv.trakt.trakt.core.comments.features.postcomment

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
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.comments.usecases.PostCommentUseCase
import kotlin.time.Duration.Companion.seconds

internal class PostCommentViewModel(
    private val mediaId: TraktId,
    private val mediaType: MediaType,
    private val sessionManager: SessionManager,
    private val postCommentUseCase: PostCommentUseCase,
) : ViewModel() {
    private val initialState = PostCommentState()

    private val loadingState = MutableStateFlow(initialState.loading)
    private val userState = MutableStateFlow(initialState.user)
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

    fun submitComment(
        comment: String,
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
                    when (mediaType) {
                        SHOW -> postCommentUseCase.postShowComment(
                            showId = mediaId,
                            comment = comment,
                            spoiler = spoiler,
                        )
                        MOVIE -> postCommentUseCase.postMovieComment(
                            movieId = mediaId,
                            comment = comment,
                            spoiler = spoiler,
                        )
                        else -> throw IllegalStateException("Invalid media type for new comment.")
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                    loadingState.update { DONE }
                }
            } finally {
                job = null
            }
        }
    }

    override fun onCleared() {
        job?.cancel()
        job = null
        super.onCleared()
    }

    val state = combine(
        loadingState,
        userState,
        resultState,
        errorState,
    ) { state ->
        PostCommentState(
            loading = state[0] as LoadingState,
            user = state[1] as User?,
            result = state[2] as Comment?,
            error = state[3] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
