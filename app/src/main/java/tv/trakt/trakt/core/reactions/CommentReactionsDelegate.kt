package tv.trakt.trakt.core.reactions

import android.content.Context
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.comments.usecases.GetCommentReactionsUseCase
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.reactions.data.ReactionSource
import tv.trakt.trakt.core.reactions.data.work.DeleteReactionWorker
import tv.trakt.trakt.core.reactions.data.work.PostReactionWorker
import tv.trakt.trakt.core.user.usecases.reactions.LoadUserReactionsUseCase
import kotlin.time.Duration.Companion.seconds

internal class CommentReactionsDelegate private constructor(
    private val appContext: Context,
    private val sessionManager: SessionManager,
    private val getCommentReactionsUseCase: GetCommentReactionsUseCase,
    private val loadUserReactionsUseCase: LoadUserReactionsUseCase,
    private val scope: CoroutineScope,
    private val source: ReactionSource,
    private val origin: ChangeOrigin,
) {
    private val summariesState = MutableStateFlow<ImmutableMap<Int, ReactionsSummary>?>(null)
    private val userReactionsState = MutableStateFlow<ImmutableMap<Int, Reaction?>?>(null)

    private var reactionJob: Job? = null

    val summaries = summariesState.asStateFlow()
    val userReactions = userReactionsState.asStateFlow()

    suspend fun loadUserReactions() {
        val userReactions = when {
            sessionManager.isAuthenticated() -> fetchUserReactions()
            else -> null
        }
        userReactionsState.update { userReactions }
    }

    fun load(commentId: Int) {
        if (summariesState.value?.containsKey(commentId) == true) {
            return
        }

        scope.launch {
            try {
                val summary = getCommentReactionsUseCase.getReactions(commentId)
                setSummary(commentId, summary)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun refresh(commentId: Int) {
        scope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    return@launch
                }

                val summary = getCommentReactionsUseCase.getReactions(commentId)
                val userReactions = fetchUserReactions()

                userReactionsState.update { userReactions }
                setSummary(commentId, summary)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun set(
        reaction: Reaction,
        commentId: Int,
    ) {
        reactionJob?.cancel()
        reactionJob = scope.launch {
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

                summariesState.update { current ->
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

    private suspend fun fetchUserReactions(): ImmutableMap<Int, Reaction?> {
        return when {
            loadUserReactionsUseCase.isLoaded() -> loadUserReactionsUseCase.loadLocalReactions()
            else -> loadUserReactionsUseCase.loadReactions()
        }
    }

    private fun setSummary(
        commentId: Int,
        summary: ReactionsSummary,
    ) {
        summariesState.update { current ->
            val mutable = current?.toMutableMap() ?: mutableMapOf()
            mutable[commentId] = summary
            mutable.toImmutableMap()
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
            source = source,
            origin = origin,
        )
    }

    private suspend fun deleteReactionWork(commentId: Int) {
        if (!sessionManager.isAuthenticated()) {
            return
        }

        DeleteReactionWorker.scheduleOneTime(
            appContext = appContext,
            commentId = commentId,
            source = source,
            origin = origin,
        )
    }

    class Factory(
        private val appContext: Context,
        private val sessionManager: SessionManager,
        private val getCommentReactionsUseCase: GetCommentReactionsUseCase,
        private val loadUserReactionsUseCase: LoadUserReactionsUseCase,
    ) {
        fun create(
            scope: CoroutineScope,
            source: ReactionSource,
            origin: ChangeOrigin,
        ): CommentReactionsDelegate {
            return CommentReactionsDelegate(
                appContext = appContext,
                sessionManager = sessionManager,
                getCommentReactionsUseCase = getCommentReactionsUseCase,
                loadUserReactionsUseCase = loadUserReactionsUseCase,
                scope = scope,
                source = source,
                origin = origin,
            )
        }
    }
}
