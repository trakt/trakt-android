package tv.trakt.trakt.core.userprofile.sections.match.ui

import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.UserMatch

internal object UserMatchPreviewData {
    val match = UserMatch(
        score = 78,
        topicsScore = 71,
        favoritesScore = 34,
        sharedTopics = persistentListOf(
            UserMatch.SharedTopic(id = 1, name = "Heist", rarity = null),
            UserMatch.SharedTopic(id = 2, name = "Space opera", rarity = 0.2F),
            UserMatch.SharedTopic(id = 3, name = "Time travel", rarity = 0.1F),
            UserMatch.SharedTopic(id = 4, name = "Folk horror", rarity = 0.7F),
            UserMatch.SharedTopic(id = 5, name = "Sitcom", rarity = 0.05F),
        ),
        sharedMovies = persistentListOf(PreviewData.movie1.ids.trakt, PreviewData.movie2.ids.trakt),
        sharedShows = persistentListOf(PreviewData.show1.ids.trakt),
    )
}
