package tv.trakt.trakt.core.summary.shows.features.comments

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
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.comments.usecases.GetCommentReactionsUseCase
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.summary.shows.features.comments.usecases.GetShowCommentsUseCase
import tv.trakt.trakt.core.user.usecase.reactions.LoadUserReactionsUseCase
import kotlin.time.Duration.Companion.seconds

internal class ShowCommentsViewModel(
    private val show: Show,
    private val getCommentsUseCase: GetShowCommentsUseCase,
    private val getCommentReactionsUseCase: GetCommentReactionsUseCase,
    private val loadUserReactionsUseCase: LoadUserReactionsUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = ShowCommentsState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val reactionsState = MutableStateFlow(initialState.reactions)
    private val userReactionsState = MutableStateFlow(initialState.userReactions)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val userState = MutableStateFlow(initialState.user)
    private val errorState = MutableStateFlow(initialState.error)

    private var reactionJob: Job? = null

    init {
        loadData()
        loadUser()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }

                coroutineScope {
                    val commentsAsync = async {
                        getCommentsUseCase.getComments(show.ids.trakt)
                    }

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
                    errorState.update { error }
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
                userReactionsState.update {
                    val mutable = it?.toMutableMap() ?: mutableMapOf()
                    mutable[commentId] = reaction
                    mutable.toImmutableMap()
                }

                if (!sessionManager.isAuthenticated()) {
                    return@launch
                }

                // Debounce to avoid multiple rapid calls.
                delay(1.seconds)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    override fun onCleared() {
        reactionJob?.cancel()
        reactionJob = null
        super.onCleared()
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ShowCommentsState> = combine(
        itemsState,
        reactionsState,
        userReactionsState,
        loadingState,
        userState,
        errorState,
    ) { state ->
        ShowCommentsState(
            items = state[0] as ImmutableList<Comment>?,
            reactions = state[1] as ImmutableMap<Int, ReactionsSummary>?,
            userReactions = state[2] as ImmutableMap<Int, Reaction?>?,
            loading = state[3] as LoadingState,
            user = state[4] as User?,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
