package tv.trakt.trakt.core.summary.shows.features.seasons.all.helpers

import android.content.Context
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.comments.usecases.GetCommentReactionsUseCase
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.reactions.data.ReactionsUpdates
import tv.trakt.trakt.core.reactions.data.ReactionsUpdates.Source.ALL_COMMENTS
import tv.trakt.trakt.core.reactions.data.work.DeleteReactionWorker
import tv.trakt.trakt.core.reactions.data.work.PostReactionWorker
import tv.trakt.trakt.core.user.usecases.reactions.LoadUserReactionsUseCase
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Owns comment-reaction state for the all-seasons screen: the per-comment reaction summaries and
 * the current user's picked reactions. Scoped to the hosting ViewModel via [scope]; the ViewModel
 * folds [commentReactions] and [userReactions] into its composed UI state and delegates the action
 * calls here.
 */
@OptIn(FlowPreview::class)
internal class SeasonReactionsController(
    private val scope: CoroutineScope,
    private val appContext: Context,
    private val getCommentReactionsUseCase: GetCommentReactionsUseCase,
    private val loadUserReactionsUseCase: LoadUserReactionsUseCase,
    private val sessionManager: SessionManager,
    reactionsUpdates: ReactionsUpdates,
) {
    private val commentReactionsState = MutableStateFlow<ImmutableMap<Int, ReactionsSummary>>(persistentMapOf())
    private val userReactionsState = MutableStateFlow<ImmutableMap<Int, Reaction?>>(persistentMapOf())

    val commentReactions = commentReactionsState.asStateFlow()
    val userReactions = userReactionsState.asStateFlow()

    private var reactionJob: Job? = null

    init {
        reactionsUpdates.observeUpdates(ALL_COMMENTS)
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                updateSeasonCommentReactions(it.first)
            }
            .launchIn(scope)
    }

    suspend fun loadUserReactions() {
        if (!sessionManager.isAuthenticated()) {
            return
        }
        try {
            val userReactions = when {
                loadUserReactionsUseCase.isLoaded() -> loadUserReactionsUseCase.loadLocalReactions()
                else -> loadUserReactionsUseCase.loadReactions()
            }
            userReactionsState.update { userReactions }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    fun loadSeasonCommentReactions(commentId: Int) {
        if (commentReactionsState.value.containsKey(commentId)) {
            return
        }

        scope.launch {
            try {
                val reactions = getCommentReactionsUseCase.getReactions(commentId)
                commentReactionsState.update { current ->
                    val mutable = current.toMutableMap()
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

    fun setSeasonCommentReaction(
        reaction: Reaction,
        commentId: Int,
    ) {
        reactionJob?.cancel()
        reactionJob = scope.launch {
            try {
                val userReaction = userReactionsState.value[commentId]

                userReactionsState.update {
                    val mutable = it.toMutableMap()
                    mutable[commentId] = when {
                        userReaction == reaction -> null
                        else -> reaction
                    }
                    mutable.toImmutableMap()
                }

                commentReactionsState.update { current ->
                    val mutable = current.toMutableMap()
                    val summary = mutable[commentId] ?: ReactionsSummary(
                        reactionsCount = 0,
                        distribution = persistentMapOf(),
                    )
                    val updatedReactions = summary.copy(
                        reactionsCount = when (userReaction) {
                            reaction -> summary.reactionsCount - 1
                            null -> summary.reactionsCount + 1
                            else -> summary.reactionsCount
                        }.coerceAtLeast(0),
                        distribution = summary.distribution
                            .toMutableMap()
                            .apply {
                                // Add or increment the picked reaction (unless toggling it off).
                                if (userReaction != reaction) {
                                    this[reaction] = (this[reaction] ?: 0) + 1
                                }
                                // Decrement the previously selected reaction, if any.
                                userReaction?.let { previous ->
                                    this[previous] = ((this[previous] ?: 0) - 1).coerceAtLeast(0)
                                }
                            }
                            .filterValues { it > 0 }
                            .toImmutableMap(),
                    )
                    mutable[commentId] = updatedReactions
                    mutable.toImmutableMap()
                }

                // Debounce to avoid multiple rapid calls.
                delay(1.seconds)
                if (reaction == userReaction) {
                    deleteReactionWork(commentId = commentId)
                } else {
                    postReactionWork(commentId = commentId, reaction = reaction)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun updateSeasonCommentReactions(commentId: Int) {
        scope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    return@launch
                }

                val reactions = getCommentReactionsUseCase.getReactions(commentId)
                val userReactions = when {
                    loadUserReactionsUseCase.isLoaded() -> loadUserReactionsUseCase.loadLocalReactions()
                    else -> loadUserReactionsUseCase.loadReactions()
                }

                userReactionsState.update { userReactions }
                commentReactionsState.update { current ->
                    val mutable = current.toMutableMap()
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
            source = ALL_COMMENTS,
        )
    }

    private suspend fun deleteReactionWork(commentId: Int) {
        if (!sessionManager.isAuthenticated()) {
            return
        }
        DeleteReactionWorker.scheduleOneTime(
            appContext = appContext,
            commentId = commentId,
            source = ALL_COMMENTS,
        )
    }
}
