package tv.trakt.trakt.core.comments

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.comments.usecases.GetCommentReactionsUseCase
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.comments.navigation.CommentsDestination
import tv.trakt.trakt.core.reactions.data.work.DeleteReactionWorker
import tv.trakt.trakt.core.reactions.data.work.PostReactionWorker
import tv.trakt.trakt.core.summary.episodes.features.comments.usecases.GetEpisodeCommentsUseCase
import tv.trakt.trakt.core.summary.movies.features.comments.usecases.GetMovieCommentsUseCase
import tv.trakt.trakt.core.summary.shows.features.comments.usecases.GetShowCommentsUseCase
import tv.trakt.trakt.core.user.usecase.reactions.LoadUserReactionsUseCase
import kotlin.time.Duration.Companion.seconds

internal class CommentsViewModel(
    savedStateHandle: SavedStateHandle,
    private val appContext: Context,
    private val sessionManager: SessionManager,
    private val getShowCommentsUseCase: GetShowCommentsUseCase,
    private val getMovieCommentsUseCase: GetMovieCommentsUseCase,
    private val getEpisodeCommentsUseCase: GetEpisodeCommentsUseCase,
    private val getCommentReactionsUseCase: GetCommentReactionsUseCase,
    private val loadUserReactionsUseCase: LoadUserReactionsUseCase,
) : ViewModel() {
    private val initialState = CommentsState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val reactionsState = MutableStateFlow(initialState.reactions)
    private val userReactionsState = MutableStateFlow(initialState.userReactions)
    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private val destination = savedStateHandle.toRoute<CommentsDestination>()
    private val mediaId = destination.mediaId.toTraktId()
    private val mediaType = MediaType.valueOf(destination.mediaType)

    private var reactionJob: Job? = null

    init {
        loadBackground()
        loadData()
        loadUser()
    }

    private fun loadBackground() {
        backgroundState.update {
            destination.mediaImage
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                coroutineScope {
                    val commentsAsync = async { fetchComments() }

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

                    val comments = commentsAsync.await()
                    val userReactions = userReactionsAsync.await()

                    itemsState.update { comments }
                    userReactionsState.update { userReactions }
                }
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

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    private suspend fun fetchComments(): ImmutableList<Comment> {
        return when (mediaType) {
            MediaType.MOVIE -> getMovieCommentsUseCase.getComments(
                movieId = mediaId,
                filter = filterState.value,
                limit = 50,
            )
            MediaType.SHOW -> getShowCommentsUseCase.getComments(
                showId = mediaId,
                filter = filterState.value,
                limit = 50,
            )
            MediaType.EPISODE -> getEpisodeCommentsUseCase.getComments(
                showId = mediaId,
                seasonEpisode = SeasonEpisode(
                    season = destination.mediaSeason ?: -1,
                    episode = destination.mediaEpisode ?: -1,
                ),
                filter = filterState.value,
                limit = 50,
            )

            else -> throw IllegalArgumentException("Unsupported media type: $mediaType")
        }
    }

    fun setFilter(filter: CommentsFilter) {
        if (loadingState.value != DONE || filter == state.value.filter) {
            return
        }

        filterState.update { filter }
        loadData()
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
                    Timber.w(error)
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
                    Timber.w(error)
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
        )
    }

    private suspend fun deleteReactionWork(commentId: Int) {
        if (!sessionManager.isAuthenticated()) {
            return
        }

        DeleteReactionWorker.scheduleOneTime(
            appContext = appContext,
            commentId = commentId,
        )
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<CommentsState> = combine(
        backgroundState,
        itemsState,
        filterState,
        reactionsState,
        userReactionsState,
        userState,
        loadingState,
        errorState,
    ) { state ->
        CommentsState(
            backgroundUrl = state[0] as String?,
            items = state[1] as ImmutableList<Comment>?,
            filter = state[2] as CommentsFilter,
            reactions = state[3] as ImmutableMap<Int, ReactionsSummary>?,
            userReactions = state[4] as ImmutableMap<Int, Reaction?>?,
            user = state[5] as User?,
            loading = state[6] as LoadingState,
            error = state[7] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
