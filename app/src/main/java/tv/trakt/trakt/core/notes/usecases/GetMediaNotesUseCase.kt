package tv.trakt.trakt.core.notes.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.UserNote
import tv.trakt.trakt.common.networking.api.v3.V3Api

internal class GetMediaNotesUseCase(
    private val v3Api: V3Api,
    private val sessionManager: SessionManager,
) {
    suspend fun getNotes(
        type: MediaType,
        mediaId: TraktId,
    ): ImmutableList<UserNote> {
        if (!sessionManager.isAuthenticated()) {
            return persistentListOf()
        }
        return v3Api.getUserNotes(type, mediaId)
            .mapNotNull(UserNote::fromDto)
            .sortedByDescending { it.updatedAt }
            .toImmutableList()
    }
}
