package tv.trakt.trakt.common.core.translations.model

import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.library.LibraryItem
import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.core.today.model.FollowingActivity
import tv.trakt.trakt.common.core.today.model.TodayForYouItem
import tv.trakt.trakt.common.core.today.model.TodayMedia
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.activity.HomeActivityItem
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.discover.DiscoverItem
import tv.trakt.trakt.common.model.profile.ProfileCommentItem
import tv.trakt.trakt.common.model.profile.ProfileRatingItem
import tv.trakt.trakt.common.model.recommended.RecommendedItem
import tv.trakt.trakt.common.networking.api.v3.model.V3BulkIntlResponse

data class LocalizedTitles(
    val movies: Map<TraktId, String> = emptyMap(),
    val shows: Map<TraktId, String> = emptyMap(),
    val episodes: Map<TraktId, String> = emptyMap(),
) {
    fun movie(movie: Movie): Movie {
        val title = movies[movie.ids.trakt] ?: return movie
        return movie.copy(title = title)
    }

    fun show(show: Show): Show {
        val title = shows[show.ids.trakt] ?: return show
        return show.copy(title = title)
    }

    fun episode(episode: Episode): Episode {
        val title = episodes[episode.ids.trakt] ?: return episode
        return episode.copy(title = title)
    }

    fun discoverItem(item: DiscoverItem): DiscoverItem {
        return when (item) {
            is DiscoverItem.MovieItem -> item.copy(movie = movie(item.movie))
            is DiscoverItem.ShowItem -> item.copy(show = show(item.show))
        }
    }

    fun recommendedItem(item: RecommendedItem): RecommendedItem {
        return when (item) {
            is RecommendedItem.MovieItem -> item.copy(movie = movie(item.movie))
            is RecommendedItem.ShowItem -> item.copy(show = show(item.show))
        }
    }

    fun watchlistItem(item: WatchlistItem): WatchlistItem {
        return when (item) {
            is WatchlistItem.MovieItem -> item.copy(movie = movie(item.movie))
            is WatchlistItem.ShowItem -> item.copy(show = show(item.show))
        }
    }

    fun calendarItem(item: CalendarItem): CalendarItem {
        return when (item) {
            is CalendarItem.EpisodeItem -> calendarItem(item)
            is CalendarItem.MovieItem -> calendarItem(item)
        }
    }

    fun calendarItem(item: CalendarItem.EpisodeItem): CalendarItem.EpisodeItem {
        return item.copy(
            show = show(item.show),
            episodes = item.episodes.map(::episode).toImmutableList(),
        )
    }

    fun calendarItem(item: CalendarItem.MovieItem): CalendarItem.MovieItem {
        return item.copy(movie = movie(item.movie))
    }

    fun activityItem(item: HomeActivityItem): HomeActivityItem {
        return when (item) {
            is HomeActivityItem.EpisodeItem -> item.copy(
                show = show(item.show),
                episode = episode(item.episode),
            )
            is HomeActivityItem.MovieItem -> item.copy(movie = movie(item.movie))
        }
    }

    fun ratingItem(item: ProfileRatingItem): ProfileRatingItem {
        return when (item) {
            is ProfileRatingItem.ShowItem -> item.copy(show = show(item.show))
            is ProfileRatingItem.MovieItem -> item.copy(movie = movie(item.movie))
            is ProfileRatingItem.EpisodeItem -> item.copy(show = show(item.show), episode = episode(item.episode))
            is ProfileRatingItem.SeasonItem -> item.copy(show = show(item.show))
        }
    }

    fun commentItem(item: ProfileCommentItem): ProfileCommentItem {
        return when (item) {
            is ProfileCommentItem.ShowItem -> item.copy(show = show(item.show))
            is ProfileCommentItem.MovieItem -> item.copy(movie = movie(item.movie))
            is ProfileCommentItem.EpisodeItem -> item.copy(show = show(item.show), episode = episode(item.episode))
        }
    }

    fun libraryItem(item: LibraryItem): LibraryItem {
        return when (item) {
            is LibraryItem.MovieItem -> item.copy(movie = movie(item.movie))
            is LibraryItem.EpisodeItem -> item.copy(show = show(item.show), episode = episode(item.episode))
        }
    }

    fun todayForYouItem(item: TodayForYouItem): TodayForYouItem {
        return when (item) {
            is TodayForYouItem.UpNext -> item.copy(show = show(item.show), episode = episode(item.episode))
            is TodayForYouItem.StartWatching -> item.copy(media = todayMedia(item.media))
        }
    }

    fun followingActivity(activity: FollowingActivity): FollowingActivity {
        val target = when (val target = activity.target) {
            is FollowingActivity.Target.MovieTarget -> target.copy(movie = movie(target.movie))
            is FollowingActivity.Target.ShowTarget -> target.copy(show = show(target.show))
            is FollowingActivity.Target.SeasonTarget -> target.copy(show = show(target.show))
            is FollowingActivity.Target.EpisodeTarget -> target.copy(
                show = show(target.show),
                episode = episode(target.episode),
            )
        }
        return activity.copy(target = target)
    }

    private fun todayMedia(media: TodayMedia): TodayMedia {
        return when (media) {
            is TodayMedia.MovieMedia -> media.copy(movie = movie(media.movie))
            is TodayMedia.ShowMedia -> media.copy(show = show(media.show))
        }
    }

    companion object {
        val Empty = LocalizedTitles()

        fun fromDto(dto: V3BulkIntlResponse): LocalizedTitles {
            return LocalizedTitles(
                movies = dto.movie.toTitles(),
                shows = dto.show.toTitles(),
                episodes = dto.episode.toTitles(),
            )
        }

        private fun Map<String, V3BulkIntlResponse.V3IntlTitle>.toTitles(): Map<TraktId, String> {
            return entries
                .mapNotNull { (id, value) ->
                    val traktId = id.toIntOrNull() ?: return@mapNotNull null
                    val title = value.title?.ifBlank { null } ?: return@mapNotNull null
                    TraktId(traktId) to title
                }
                .toMap()
        }
    }
}
