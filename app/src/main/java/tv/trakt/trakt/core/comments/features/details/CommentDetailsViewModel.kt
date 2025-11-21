package tv.trakt.trakt.core.comments.features.details

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.comments.usecases.GetCommentReactionsUseCase
import tv.trakt.trakt.common.core.comments.usecases.GetCommentRepliesUseCase
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.reactions.data.ReactionsUpdates.Source
import tv.trakt.trakt.core.reactions.data.work.DeleteReactionWorker
import tv.trakt.trakt.core.reactions.data.work.PostReactionWorker
import tv.trakt.trakt.core.user.usecases.reactions.LoadUserReactionsUseCase
import kotlin.time.Duration.Companion.seconds

internal class CommentDetailsViewModel(
    private val appContext: Context,
    private val comment: Comment,
    private val sessionManager: SessionManager,
    private val getRepliesUseCase: GetCommentRepliesUseCase,
    private val getCommentReactionsUseCase: GetCommentReactionsUseCase,
    private val loadUserReactionsUseCase: LoadUserReactionsUseCase,
) : ViewModel() {
    private val initialState = CommentDetailsState()

    private val commentState = MutableStateFlow(comment)
    private val commentReplies = MutableStateFlow(initialState.replies)
    private val reactionsState = MutableStateFlow(initialState.reactions)
    private val userReactionsState = MutableStateFlow(initialState.userReactions)
    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var reactionJob: Job? = null

    init {
        loadUser()
        loadData()
        loadReactions(comment.id)
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

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                coroutineScope {
                    val repliesAsync = async { getRepliesUseCase.getCommentReplies(comment.id) }

                    val userReactionsAsync = async {
                        if (!sessionManager.isAuthenticated()) {
                            return@async null
                        }
                        if (loadUserReactionsUseCase.isLoaded()) {
                            loadUserReactionsUseCase.loadLocalReactions()
                        } else {
                            loadUserReactionsUseCase.loadReactions()
                        }
                    }

                    val replies = repliesAsync.await()
                    val userReactions = userReactionsAsync.await()

                    commentReplies.update { replies }
                    userReactionsState.update { userReactions }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.value = error
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    fun loadReactions(commentId: Int) {
        viewModelScope.launch {
            if (reactionsState.value?.containsKey(commentId) == true) {
                // Reactions already loaded for this comment.
                return@launch
            }

            try {
                val reactions = getCommentReactionsUseCase.getReactions(commentId)
                reactionsState.update { current ->
                    val mutable = current?.toMutableMap() ?: mutableMapOf()
                    mutable[commentId] = reactions
                    mutable.toImmutableMap()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun setReaction(
        reaction: Reaction,
        commentId: Int,
    ) {
        reactionJob?.cancel()
        reactionJob = viewModelScope.launch {
            try {
                val userReaction = userReactionsState.value?.get(commentId)

                userReactionsState.update {
                    val mutable = it?.toMutableMap() ?: mutableMapOf()
                    mutable[commentId] = when {
                        userReaction == reaction -> null
                        else -> reaction
                    }
                    mutable.toImmutableMap()
                }

                reactionsState.update { current ->
                    val mutable = current?.toMutableMap() ?: mutableMapOf()

                    mutable[commentId]?.let {
                        val updatedReactions = it.copy(
                            reactionsCount = when (userReaction) {
                                reaction -> it.reactionsCount - 1
                                null -> it.reactionsCount + 1
                                else -> it.reactionsCount
                            },
                            distribution = it.distribution
                                .toMutableMap()
                                .apply {
                                    for ((r, count) in entries) {
                                        if (r == reaction && userReaction != reaction) {
                                            this[r] = count + 1
                                        } else if (r == userReaction) {
                                            this[r] = (count - 1).coerceAtLeast(0)
                                        }
                                    }
                                }
                                .toImmutableMap(),
                        )
                        mutable[commentId] = updatedReactions
                    }

                    mutable.toImmutableMap()
                }

                // Debounce to avoid multiple rapid calls.
                delay(1.seconds)
                if (reaction == userReaction) {
                    deleteReactionWork(
                        commentId = commentId,
                    )
                } else {
                    postReactionWork(
                        commentId = commentId,
                        reaction = reaction,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private suspend fun postReactionWork(
        commentId: Int,
        reaction: Reaction,
    ) {
        if (!sessionManager.isAuthenticated()) {
            return
        }

        PostReactionWorker.scheduleOneTime(
            appContext = appContext,
            commentId = commentId,
            reaction = reaction,
            source = Source.COMMENT_DETAILS,
        )
    }

    private suspend fun deleteReactionWork(commentId: Int) {
        if (!sessionManager.isAuthenticated()) {
            return
        }

        DeleteReactionWorker.scheduleOneTime(
            appContext = appContext,
            commentId = commentId,
            source = Source.COMMENT_DETAILS,
        )
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<CommentDetailsState> = combine(
        commentState,
        commentReplies,
        reactionsState,
        userReactionsState,
        userState,
        loadingState,
        errorState,
    ) { state ->
        CommentDetailsState(
            comment = state[0] as Comment?,
            replies = state[1] as ImmutableList<Comment>?,
            reactions = state[2] as ImmutableMap<Int, ReactionsSummary>?,
            userReactions = state[3] as ImmutableMap<Int, Reaction?>?,
            user = state[4] as User?,
            loading = state[5] as LoadingState,
            error = state[6] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
