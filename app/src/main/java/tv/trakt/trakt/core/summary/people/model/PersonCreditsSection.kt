package tv.trakt.trakt.core.summary.people.model

import androidx.annotation.Keep
import tv.trakt.trakt.common.model.MediaMode

@Keep
internal enum class PersonCreditsSection(
    val initialMode: MediaMode,
    val screenName: String,
    val drilldownSource: String,
) {
    Shows(
        initialMode = MediaMode.Shows,
        screenName = "all_person_shows",
        drilldownSource = "credits",
    ),
    Movies(
        initialMode = MediaMode.Movies,
        screenName = "all_person_movies",
        drilldownSource = "credits",
    ),
    History(
        initialMode = MediaMode.Media,
        screenName = "all_person_history",
        drilldownSource = "credits-history",
    ),
    ;

    val isHistory: Boolean
        get() = this == History
}
