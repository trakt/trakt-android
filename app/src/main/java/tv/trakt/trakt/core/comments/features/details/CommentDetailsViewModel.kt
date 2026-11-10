@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.comments.features.details

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.comments.usecases.GetCommentReactionsUseCase
import tv.trakt.trakt.common.core.comments.usecases.GetCommentRepliesUseCase
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.features.translation.data.CommentTranslationsStore
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslationEvent
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslationEvent.OpenExternalTranslation
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslations
import tv.trakt.trakt.core.reactions.CommentReactionsDelegate
import tv.trakt.trakt.core.reactions.data.ReactionSource
import tv.trakt.trakt.core.reactions.data.work.DeleteReactionWorker
import tv.trakt.trakt.core.reactions.data.work.PostReactionWorker
import tv.trakt.trakt.core.user.usecases.reactions.LoadUserReactionsUseCase
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class CommentDetailsViewModel(
    private val comment: Comment,
    private val sessionManager: SessionManager,
    private val getRepliesUseCase: GetCommentRepliesUseCase,
    private val dataChanges: DataChanges,
    private val translationsStore: CommentTranslationsStore,
    private val analytics: Analytics,
    reactionsFactory: CommentReactionsDelegate.Factory,
) : ViewModel() {
    private val initialState = CommentDetailsState()
    private val origin = ChangeOrigin.unique()

    private val commentState = MutableStateFlow(comment)
    private val commentReplies = MutableStateFlow(initialState.replies)
    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val reactions = reactionsFactory.create(
        scope = viewModelScope,
        source = ReactionSource.CommentDetails,
        origin = origin,
    )

    private var loadingJob: Job? = null

    init {
        loadData()
        loadReactions(comment.id)
    }

    private suspend fun loadUser() {
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

    private fun loadData() {
        loadingJob = viewModelScope.launch {
            delay(750.milliseconds)
            loadingState.update { Loading }
        }

        viewModelScope.launch {
            try {
                loadUser()
                coroutineScope {
                    val repliesAsync = async {
                        getRepliesUseCase.getCommentReplies(comment.id)
                    }
                    val userReactionsAsync = async { reactions.loadUserReactions() }

                    val replies = repliesAsync.await()
                    userReactionsAsync.await()

                    commentReplies.update { replies }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.value = error
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                loadingJob?.cancel()
            }
        }
    }

    fun updateComment(comment: Comment) {
        commentState.update { comment }
        dataChanges.notify(Kind.Comments, origin = origin)
    }

    fun logSpoilerReveal() {
        analytics.comments.logSpoilerReveal()
    }

    fun toggleTranslation(comment: Comment) {
        viewModelScope.launch {
            if (!translationsStore.toggle(comment)) {
                events.emit(OpenExternalTranslation(comment.comment.trim()))
            }
        }
    }

    fun addReply(comment: Comment) {
        commentReplies.update { current ->
            val mutable = current?.toMutableList() ?: mutableListOf()
            mutable.add(comment)
            mutable.toImmutableList()
        }

        dataChanges.notify(Kind.Comments, origin = origin)
    }

    fun deleteReply(replyId: Int) {
        commentReplies.update { current ->
            current?.filterNot { it.id == replyId }?.toImmutableList()
        }

        dataChanges.notify(Kind.Comments, origin = origin)
    }

    fun loadReactions(commentId: Int) {
        reactions.load(commentId)
    }

    fun setReaction(
        reaction: Reaction,
        commentId: Int,
    ) {
        reactions.set(reaction, commentId)
    }

    val events: Flow<CommentTranslationEvent>
        field = MutableSharedFlow<CommentTranslationEvent>(replay = 0)

    val state = combine(
        commentState,
        commentReplies,
        reactions.summaries,
        reactions.userReactions,
        userState,
        loadingState,
        errorState,
        translationsStore.translations,
    ) { state ->
        CommentDetailsState(
            comment = state[0] as Comment?,
            replies = state[1] as ImmutableList<Comment>?,
            reactions = state[2] as ImmutableMap<Int, ReactionsSummary>?,
            userReactions = state[3] as ImmutableMap<Int, Reaction?>?,
            user = state[4] as User?,
            loading = state[5] as LoadingState,
            error = state[6] as Exception?,
            translations = state[7] as CommentTranslations,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
