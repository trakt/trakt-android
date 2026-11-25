package tv.trakt.trakt.core.lists.sheets.collaborators.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.ListsCollaborationsLocalSource
import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto

internal class ManageListCollaboratorsUseCase(
    private val remoteSource: ListsRemoteSource,
    private val localSource: ListsCollaborationsLocalSource,
) {
    suspend fun getCollaborators(listId: TraktId): ImmutableList<User> {
        return remoteSource.getCollaborators(listId)
            .asyncMap { User.fromDto(it) }
            .toImmutableList()
    }

    /**
     * @return false when the list has reached its collaborator limit.
     */
    suspend fun addCollaborator(
        listId: TraktId,
        user: User,
    ): Boolean {
        val added = remoteSource.addCollaborator(listId, user.collaboratorSlug)
        if (added) {
            localSource.notifyUpdate()
        }
        return added
    }

    suspend fun removeCollaborator(
        listId: TraktId,
        user: User,
    ) {
        remoteSource.removeCollaborator(listId, user.collaboratorSlug)
        localSource.notifyUpdate()
    }

    private val User.collaboratorSlug: String
        get() = ids.slug.value.ifBlank { username }
}
