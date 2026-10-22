package tv.trakt.trakt.core.trivia.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

internal enum class TriviaCategory(
    val value: String,
    @param:StringRes val displayRes: Int,
) {
    BehindTheScenes(
        "bts",
        R.string.option_text_trivia_category_bts,
    ),
    CastAndCrew(
        "cast_n_crew",
        R.string.option_text_trivia_category_cast_n_crew,
    ),
    StoryAndThemes(
        "story_n_themes",
        R.string.option_text_trivia_category_story_n_themes,
    ),
    ImpactAndLegacy(
        "impact_n_legacy",
        R.string.option_text_trivia_category_impact_n_legacy,
    ),
    RealWorldConnections(
        "real_world_connections",
        R.string.option_text_trivia_category_real_world_connections,
    ),
    ;

    companion object {
        fun fromValue(value: String): TriviaCategory? {
            return entries.firstOrNull { it.value == value }
        }
    }
}
