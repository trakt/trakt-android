package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.networking.api.v3.model.V3UserNoteResponse
import java.time.ZonedDateTime

@Immutable
data class UserNote(
    val id: Int,
    val text: String,
    val type: NoteType,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
) {
    companion object {
        fun fromDto(dto: V3UserNoteResponse): UserNote? {
            return UserNote(
                id = dto.id,
                text = dto.notes,
                type = NoteType.fromValue(dto.type) ?: return null,
                createdAt = dto.createdAt.toZonedDateTime(),
                updatedAt = dto.updatedAt.toZonedDateTime(),
            )
        }
    }
}

enum class NoteType(
    val value: String,
) {
    Favorites("favorites"),
    Note("note"),
    ;

    companion object {
        fun fromValue(value: String): NoteType? {
            return entries.firstOrNull { it.value == value }
        }
    }
}
