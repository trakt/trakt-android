package tv.trakt.trakt.core.comments

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
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
import tv.trakt.trakt.core.comments.usecases.GetCommentsFilterUseCase
import tv.trakt.trakt.core.reactions.data.ReactionsUpdates
import tv.trakt.trakt.core.reactions.data.ReactionsUpdates.Source
import tv.trakt.trakt.core.reactions.data.work.DeleteReactionWorker
import tv.trakt.trakt.core.reactions.data.work.PostReactionWorker
import tv.trakt.trakt.core.summary.episodes.features.comments.usecases.GetEpisodeCommentsUseCase
import tv.trakt.trakt.core.summary.movies.features.comments.usecases.GetMovieCommentsUseCase
import tv.trakt.trakt.core.summary.shows.features.comments.usecases.GetShowCommentsUseCase
import tv.trakt.trakt.core.user.usecases.reactions.LoadUserReactionsUseCase
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
internal class CommentsViewModel(
    savedStateHandle: SavedStateHandle,
    private val appContext: Context,
    private val sessionManager: SessionManager,
    private val getFilterUseCase: GetCommentsFilterUseCase,
    private val getShowCommentsUseCase: GetShowCommentsUseCase,
    private val getMovieCommentsUseCase: GetMovieCommentsUseCase,
    private val getEpisodeCommentsUseCase: GetEpisodeCommentsUseCase,
    private val getCommentReactionsUseCase: GetCommentReactionsUseCase,
    private val loadUserReactionsUseCase: LoadUserReactionsUseCase,
    private val reactionsUpdates: ReactionsUpdates,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<CommentsDestination>()
    private val initialState = CommentsState()

    private val mediaState = MutableStateFlow(
        CommentsState.MediaState(
            id = destination.mediaId.toTraktId(),
            type = destination.mediaType,
        ),
    )

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(destination.initialFilter)
    private val reactionsState = MutableStateFlow(initialState.reactions)
    private val userReactionsState = MutableStateFlow(initialState.userReactions)
    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var reactionJob: Job? = null

    init {
        loadBackground()
        loadData()
        loadUser()
        observeData()
    }

    private fun observeData() {
        reactionsUpdates.observeUpdates(Source.COMMENT_DETAILS)
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                updateReactions(it.first)
            }
            .launchIn(viewModelScope)
    }

    private fun loadBackground() {
        backgroundState.update {
            destination.mediaImage
        }
    }

    private suspend fun loadFilter(): CommentsFilter {
        val filter = getFilterUseCase.getFilter()
        filterState.update { filter }
        return filter
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
                    Timber.recordError(error)
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
                    Timber.recordError(error)
                }
            }
        }
    }

    private suspend fun fetchComments(): ImmutableList<Comment> {
        val filter = loadFilter()
        return when (destination.mediaType) {
            MediaType.MOVIE -> getMovieCommentsUseCase.getComments(
                movieId = destination.mediaId.toTraktId(),
                filter = filter,
                limit = 50,
            )
            MediaType.SHOW -> getShowCommentsUseCase.getComments(
                showId = destination.mediaId.toTraktId(),
                filter = filter,
                limit = 50,
            )
            MediaType.EPISODE -> getEpisodeCommentsUseCase.getComments(
                showId = destination.mediaId.toTraktId(),
                seasonEpisode = SeasonEpisode(
                    season = destination.mediaSeason ?: -1,
                    episode = destination.mediaEpisode ?: -1,
                ),
                filter = filter,
                limit = 50,
            )

            else -> throw IllegalArgumentException("Unsupported media type: ${destination.mediaType}")
        }
    }

    fun setFilter(filter: CommentsFilter) {
        if (loadingState.value != DONE || filter == filterState.value) {
            return
        }
        viewModelScope.launch {
            getFilterUseCase.setFilter(filter)
            loadFilter()
            loadData()
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

    fun addComment(comment: Comment) {
        itemsState.update {
            val mutable = it?.toMutableList() ?: mutableListOf()
            mutable.add(0, comment)
            mutable.toImmutableList()
        }
    }

    private fun updateReactions(commentId: Int) {
        viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    return@launch
                }

                val reactions = getCommentReactionsUseCase.getReactions(commentId)
                val userReactions = when {
                    loadUserReactionsUseCase.isLoaded() -> {
                        loadUserReactionsUseCase.loadLocalReactions()
                    }
                    else -> loadUserReactionsUseCase.loadReactions()
                }

                userReactionsState.update { userReactions }
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
            source = Source.ALL_COMMENTS,
        )
    }

    private suspend fun deleteReactionWork(commentId: Int) {
        if (!sessionManager.isAuthenticated()) {
            return
        }

        DeleteReactionWorker.scheduleOneTime(
            appContext = appContext,
            commentId = commentId,
            source = Source.ALL_COMMENTS,
        )
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<CommentsState> = combine(
        backgroundState,
        mediaState,
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
            media = state[1] as CommentsState.MediaState?,
            items = state[2] as ImmutableList<Comment>?,
            filter = state[3] as CommentsFilter,
            reactions = state[4] as ImmutableMap<Int, ReactionsSummary>?,
            userReactions = state[5] as ImmutableMap<Int, Reaction?>?,
            user = state[6] as User?,
            loading = state[7] as LoadingState,
            error = state[8] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
