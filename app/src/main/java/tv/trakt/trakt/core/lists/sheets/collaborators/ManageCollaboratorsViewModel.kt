package tv.trakt.trakt.core.lists.sheets.collaborators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sheets.collaborators.usecases.ManageListCollaboratorsUseCase
import tv.trakt.trakt.core.user.usecases.social.LoadUserSocialUseCase

internal sealed interface ManageCollaboratorsEvent {
    data class ListMoved(
        val destination: PersonalListType,
    ) : ManageCollaboratorsEvent

    data object LimitReached : ManageCollaboratorsEvent

    data object Failed : ManageCollaboratorsEvent
}

@Suppress("UNCHECKED_CAST")
internal class ManageCollaboratorsViewModel(
    private val list: CustomList,
    private val manageCollaboratorsUseCase: ManageListCollaboratorsUseCase,
    private val loadSocialUseCase: LoadUserSocialUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ManageCollaboratorsState()

    private val followersState = MutableStateFlow<ImmutableList<User>>(EmptyImmutableList)
    private val followingState = MutableStateFlow<ImmutableList<User>>(EmptyImmutableList)
    private val collaboratorsState = MutableStateFlow<ImmutableList<User>>(EmptyImmutableList)
    private val loadingState = MutableStateFlow<LoadingState>(initialState.loading)
    private val pendingUserState = MutableStateFlow(initialState.pendingUserId)

    private val eventsFlow = MutableSharedFlow<ManageCollaboratorsEvent>(
        replay = 0,
        extraBufferCapacity = 1,
    )
    val events = eventsFlow.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                coroutineScope {
                    val followers = async { loadSocialUseCase.loadFollowers() }
                    val following = async { loadSocialUseCase.loadFollowing() }
                    val collaborators = async { manageCollaboratorsUseCase.getCollaborators(list.ids.trakt) }

                    val loadedFollowers = followers.await()
                    val loadedFollowing = following.await()
                    val loadedCollaborators = collaborators.await()

                    followersState.update { loadedFollowers }
                    followingState.update { loadedFollowing }
                    collaboratorsState.update { loadedCollaborators }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    eventsFlow.tryEmit(ManageCollaboratorsEvent.Failed)
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun addCollaborator(user: User) {
        if (pendingUserState.value != null) {
            return
        }

        viewModelScope.launch {
            try {
                pendingUserState.update { user.ids.trakt }
                val previous = collaboratorsState.value

                val added = manageCollaboratorsUseCase.addCollaborator(list.ids.trakt, user)
                if (!added) {
                    analytics.lists.logListCollaborator(ACTION_LIMIT_REACHED)
                    eventsFlow.emit(ManageCollaboratorsEvent.LimitReached)
                    return@launch
                }

                analytics.lists.logListCollaborator(ACTION_ADD)
                collaboratorsState.update { (it + user).toImmutableList() }

                if (previous.isEmpty()) {
                    eventsFlow.emit(ManageCollaboratorsEvent.ListMoved(PersonalListType.Collaborations))
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    eventsFlow.tryEmit(ManageCollaboratorsEvent.Failed)
                    Timber.recordError(error)
                }
            } finally {
                pendingUserState.update { null }
            }
        }
    }

    fun removeCollaborator(user: User) {
        if (pendingUserState.value != null) {
            return
        }

        viewModelScope.launch {
            try {
                pendingUserState.update { user.ids.trakt }
                val previous = collaboratorsState.value

                manageCollaboratorsUseCase.removeCollaborator(list.ids.trakt, user)

                analytics.lists.logListCollaborator(ACTION_REMOVE)
                collaboratorsState.update { current ->
                    current.filterNot { it.ids.trakt == user.ids.trakt }.toImmutableList()
                }

                if (previous.size == 1) {
                    eventsFlow.emit(ManageCollaboratorsEvent.ListMoved(PersonalListType.Personal))
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    eventsFlow.tryEmit(ManageCollaboratorsEvent.Failed)
                    Timber.recordError(error)
                }
            } finally {
                pendingUserState.update { null }
            }
        }
    }

    val state = combine(
        followersState,
        followingState,
        collaboratorsState,
        loadingState,
        pendingUserState,
    ) { state ->
        ManageCollaboratorsState(
            candidates = buildCollaboratorCandidates(
                followers = state[0] as ImmutableList<User>,
                following = state[1] as ImmutableList<User>,
                collaborators = state[2] as ImmutableList<User>,
            ),
            loading = state[3] as LoadingState,
            pendingUserId = state[4] as TraktId?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )

    private companion object {
        const val ACTION_ADD = "add"
        const val ACTION_REMOVE = "remove"
        const val ACTION_LIMIT_REACHED = "limit-reached"
    }
}
