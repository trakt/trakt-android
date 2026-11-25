package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.serializers.InstantSerializer
import tv.trakt.trakt.common.networking.UserCommentsDto
import java.time.Instant

@Immutable
@Serializable
data class VipVeteran(
    @Serializable(InstantSerializer::class)
    val since: Instant,
    val years: Int,
    val title: Title?,
    @Serializable(InstantSerializer::class)
    val graceEndsAt: Instant?,
) {
    @Serializable
    enum class Title(
        val value: String,
    ) {
        Pro("pro"),
        Veteran("veteran"),
        Elite("elite"),
        Legend("legend"),
        ;

        companion object {
            fun fromValue(value: String?): Title? = entries.firstOrNull { it.value == value }
        }
    }

    companion object {
        fun fromDto(dto: UserCommentsDto): VipVeteran? {
            if (dto.vipVeteranTier == null) return null
            val since = dto.vipVeteranSince?.toInstantOrNull() ?: return null

            return VipVeteran(
                since = since,
                years = dto.vipVeteranYears ?: 0,
                title = Title.fromValue(dto.vipVeteranTitle),
                graceEndsAt = dto.vipGraceEndsAt?.toInstantOrNull(),
            )
        }

        private fun String.toInstantOrNull(): Instant? = runCatching { toInstant() }.getOrNull()
    }
}
