package tv.trakt.trakt.core.today.model

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface TodayStoryFrame {
    val key: String
    val media: TodayMedia

    @Immutable
    sealed interface Title : TodayStoryFrame {
        val story: TodayTitleStory

        override val media: TodayMedia
            get() = story.media
    }

    /** Overview of everyone's activity, shown first when 2+ friends acted on the title. */
    @Immutable
    data class Summary(
        override val story: TodayTitleStory,
    ) : Title {
        override val key = "${story.key}-summary"
    }

    @Immutable
    data class Friend(
        override val story: TodayTitleStory,
        val action: TodayFriendAction,
    ) : Title {
        override val key = "${story.key}-${action.key}"
    }

    @Immutable
    data class ForYou(
        val item: TodayForYouItem,
    ) : TodayStoryFrame {
        override val key = item.key
        override val media = item.media
    }
}
