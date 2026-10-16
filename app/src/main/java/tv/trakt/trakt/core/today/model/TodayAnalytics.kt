package tv.trakt.trakt.core.today.model

internal val TodayStoryGroup.analyticsType: String
    get() = when (this) {
        is TodayStoryGroup.Title -> "title"
        is TodayStoryGroup.ForYou -> "for-you"
    }

internal val TodayStoryFrame.analyticsType: String
    get() = when (this) {
        is TodayStoryFrame.Summary -> "summary"
        is TodayStoryFrame.Friend -> "friend"
        is TodayStoryFrame.ForYou -> "for-you"
    }
