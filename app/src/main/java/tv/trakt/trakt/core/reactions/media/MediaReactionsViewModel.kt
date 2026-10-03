package tv.trakt.trakt.core.reactions.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.common.model.reactions.MediaReactionsSummary
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.core.reactions.media.usecases.DeleteMediaReactionUseCase
import tv.trakt.trakt.core.reactions.media.usecases.GetMediaReactionsSummaryUseCase
import tv.trakt.trakt.core.reactions.media.usecases.LoadUserMediaReactionsUseCase
import tv.trakt.trakt.core.reactions.media.usecases.PostMediaReactionUseCase

@Suppress("UNCHECKED_CAST")
internal class MediaReactionsViewModel(
    private val target: MediaReactionsTarget,
    private val sessionManager: SessionManager,
    private val getSummaryUseCase: GetMediaReactionsSummaryUseCase,
    private val loadUserReactionsUseCase: LoadUserMediaReactionsUseCase,
    private val postReactionUseCase: PostMediaReactionUseCase,
    private val deleteReactionUseCase: DeleteMediaReactionUseCase,
) : ViewModel() {
    private val initialState = MediaReactionsState()

    private val userState = MutableStateFlow(initialState.user)
    private val summaryState = MutableStateFlow(initialState.summary)
    private val userReactionsState = MutableStateFlow(initialState.userReactions)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private val writeMutex = Mutex()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val user = sessionManager.getProfile()
                val summaryAsync = async { getSummaryUseCase.getSummary(target) }
                val userReactionsAsync = async { loadUserReactions(user) }

                userState.update { user }
                summaryState.update { summaryAsync.await() }
                userReactionsState.update { userReactionsAsync.await() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private suspend fun loadUserReactions(user: User?): ImmutableList<MediaReaction> {
        if (user == null) return initialState.userReactions

        val reactions = when {
            loadUserReactionsUseCase.isLoaded(target) -> loadUserReactionsUseCase.loadLocalReactions(target)
            else -> loadUserReactionsUseCase.loadReactions(target)
        }

        return reactions
            .map { it.reaction }
            .distinct()
            .toImmutableList()
    }

    fun toggleReaction(reaction: MediaReaction) {
        if (userState.value == null) return

        val current = userReactionsState.value
        val isRemoving = reaction in current
        if (!isRemoving && current.size >= MediaReaction.MAX_PER_MEDIA) return

        userReactionsState.update {
            when {
                isRemoving -> it - reaction
                else -> it + reaction
            }.toImmutableList()
        }
        summaryState.update {
            it.withUserChange(
                reaction = reaction,
                isRemoving = isRemoving,
                heldBefore = current.size,
            )
        }

        viewModelScope.launch {
            writeMutex.withLock {
                try {
                    when {
                        isRemoving -> {
                            deleteReactionUseCase.deleteReaction(target, reaction)
                        }
                        else -> {
                            postReactionUseCase.postReaction(target, reaction)
                        }
                    }
                } catch (error: Exception) {
                    error.rethrowCancellation {
                        errorState.update { error }
                        Timber.recordError(error)
                    }
                    restoreFromRemote()
                }
            }
        }
    }

    fun clearError() {
        errorState.update { null }
    }

    private suspend fun restoreFromRemote() {
        try {
            val summary = getSummaryUseCase.getSummary(target)
            val userReactions = loadUserReactionsUseCase.loadReactions(target)
                .map { it.reaction }
                .distinct()
                .toImmutableList()

            summaryState.update { summary }
            userReactionsState.update { userReactions }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    val state = combine(
        userState,
        summaryState,
        userReactionsState,
        loadingState,
        errorState,
    ) { state ->
        MediaReactionsState(
            user = state[0] as User?,
            summary = state[1] as MediaReactionsSummary,
            userReactions = state[2] as ImmutableList<MediaReaction>,
            loading = state[3] as LoadingState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

private fun MediaReactionsSummary.withUserChange(
    reaction: MediaReaction,
    isRemoving: Boolean,
    heldBefore: Int,
): MediaReactionsSummary {
    val delta = if (isRemoving) -1 else 1
    val heldAfter = heldBefore + delta
    val usersDelta = when {
        heldBefore == 0 && heldAfter > 0 -> 1
        heldBefore > 0 && heldAfter == 0 -> -1
        else -> 0
    }

    return copy(
        reactionsCount = (reactionsCount + delta).coerceAtLeast(0),
        usersCount = (usersCount + usersDelta).coerceAtLeast(0),
        distribution = distribution
            .plus(reaction to ((distribution[reaction] ?: 0) + delta).coerceAtLeast(0))
            .toImmutableMap(),
    )
}
