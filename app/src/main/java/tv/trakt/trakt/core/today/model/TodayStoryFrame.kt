package tv.trakt.trakt.core.today.model

import androidx.compose.runtime.Immutable

/** One page of the story viewer. */
@Immutable
internal sealed interface TodayStoryFrame {
    val key: String
    val story: TodayTitleStory

    /** Overview of everyone's activity, shown first when 2+ friends acted on the title. */
    @Immutable
    data class Summary(
        override val story: TodayTitleStory,
    ) : TodayStoryFrame {
        override val key = "${story.key}-summary"
    }

    @Immutable
    data class Friend(
        override val story: TodayTitleStory,
        val action: TodayFriendAction,
    ) : TodayStoryFrame {
        override val key = "${story.key}-${action.key}"
    }
}
