package tv.trakt.trakt.core.lists.sheets.collaborators

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal data class ManageCollaboratorsState(
    val candidates: ImmutableList<CollaboratorCandidate> = EmptyImmutableList,
    val loading: LoadingState = Idle,
    val pendingUserId: TraktId? = null,
)
