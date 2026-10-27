package tv.trakt.trakt.common.model.lists

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class ParentalGuideCategory(
    val apiKey: String,
    @param:StringRes val displayRes: Int,
) {
    Nudity("parental_nudity", R.string.label_parental_guide_category_nudity),
    Violence("parental_violence", R.string.label_parental_guide_category_violence),
    Profanity("parental_profanity", R.string.label_parental_guide_category_profanity),
    Alcohol("parental_alcohol", R.string.label_parental_guide_category_alcohol),
    Frightening("parental_frightening", R.string.label_parental_guide_category_frightening),
}

enum class ParentalGuideSeverity(
    @param:StringRes val displayRes: Int,
) {
    None(R.string.label_parental_guide_severity_none),
    Mild(R.string.label_parental_guide_severity_mild),
    Moderate(R.string.label_parental_guide_severity_moderate),
    Severe(R.string.label_parental_guide_severity_severe),
    ;

    companion object {
        val Min = None.ordinal
        val Max = Severe.ordinal

        fun fromValue(value: Int): ParentalGuideSeverity = entries.getOrElse(value) { Severe }
    }
}
