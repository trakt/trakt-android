package tv.trakt.trakt.common.model.lists

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.networking.LanguageDto

@Immutable
data class SmartListLanguage(
    val code: String,
    val name: String,
) {
    companion object {
        fun fromDto(dto: LanguageDto): SmartListLanguage {
            return SmartListLanguage(
                code = dto.code,
                name = dto.name,
            )
        }
    }
}
