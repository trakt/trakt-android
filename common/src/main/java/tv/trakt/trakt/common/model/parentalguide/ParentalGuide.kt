package tv.trakt.trakt.common.model.parentalguide

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.networking.api.v3.model.V3ParentalGuideResponse
import tv.trakt.trakt.resources.R

@Immutable
data class ParentalGuide(
    val severities: ImmutableMap<ParentalGuideCategory, ParentalGuideSeverity> = persistentMapOf(),
) {
    val isEmpty: Boolean
        get() = severities.isEmpty()

    companion object {
        fun fromDto(dto: V3ParentalGuideResponse): ParentalGuide {
            return ParentalGuide(
                severities = dto.guide
                    .mapNotNull { entry ->
                        val category = ParentalGuideCategory.fromValue(entry.category)
                        val severity = ParentalGuideSeverity.fromValue(entry.severity)
                        if (category == null || severity == null) {
                            return@mapNotNull null
                        }
                        category to severity
                    }
                    .toMap()
                    .toImmutableMap(),
            )
        }
    }
}

/**
 * Declaration order is render order.
 */
enum class ParentalGuideCategory(
    val value: String,
    @param:StringRes val displayTextRes: Int,
) {
    Nudity("NUDITY", R.string.label_parental_guide_category_nudity),
    Violence("VIOLENCE", R.string.label_parental_guide_category_violence),
    Profanity("PROFANITY", R.string.label_parental_guide_category_profanity),
    Alcohol("ALCOHOL", R.string.label_parental_guide_category_alcohol),
    Frightening("FRIGHTENING", R.string.label_parental_guide_category_frightening),
    ;

    companion object {
        fun fromValue(value: String): ParentalGuideCategory? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

enum class ParentalGuideSeverity(
    val value: String,
    @param:StringRes val displayTextRes: Int,
) {
    None("NONE", R.string.label_parental_guide_severity_none),
    Mild("MILD", R.string.label_parental_guide_severity_mild),
    Moderate("MODERATE", R.string.label_parental_guide_severity_moderate),
    Severe("SEVERE", R.string.label_parental_guide_severity_severe),
    ;

    companion object {
        fun fromValue(value: String): ParentalGuideSeverity? {
            return entries.firstOrNull { it.value == value }
        }
    }
}
