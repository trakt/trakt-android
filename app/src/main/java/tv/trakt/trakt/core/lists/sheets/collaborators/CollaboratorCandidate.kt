package tv.trakt.trakt.core.lists.sheets.collaborators

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.User

@Immutable
internal data class CollaboratorCandidate(
    val user: User,
    val isCollaborator: Boolean,
    val isMutual: Boolean,
)

internal fun buildCollaboratorCandidates(
    followers: List<User>,
    following: List<User>,
    collaborators: List<User>,
): ImmutableList<CollaboratorCandidate> {
    val collaboratorIds = collaborators.mapTo(mutableSetOf()) { it.ids.trakt }
    val followerIds = followers.mapTo(mutableSetOf()) { it.ids.trakt }
    val followingIds = following.mapTo(mutableSetOf()) { it.ids.trakt }
    val unfollowedCollaborators = collaborators.filter { it.ids.trakt !in followerIds }

    return (followers + unfollowedCollaborators)
        .map {
            CollaboratorCandidate(
                user = it,
                isCollaborator = it.ids.trakt in collaboratorIds,
                isMutual = it.ids.trakt in followingIds,
            )
        }
        .sortedBy { candidate ->
            when {
                candidate.isCollaborator -> 0
                candidate.isMutual -> 1
                else -> 2
            }
        }
        .toImmutableList()
}
