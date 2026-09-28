package tv.trakt.trakt.app.common.preview

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.ProgressShow
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode

internal object StatusPreviewData {
    private val episodes = persistentListOf(
        PreviewData.newEpisode,
        PreviewData.newPremiereEpisode,
        PreviewData.newFinaleEpisode,
        PreviewData.premiereEpisode,
        PreviewData.finaleEpisode,
    )

    val upcomingEpisodes: ImmutableList<HomeUpcomingItem.EpisodeItem> = episodes
        .map { it.toUpcomingItem() }
        .toImmutableList()

    val upcomingItems: ImmutableList<HomeUpcomingItem> = (
        upcomingEpisodes + HomeUpcomingItem.MovieItem(movie = PreviewData.newMovie)
    ).toImmutableList()

    val progressShows: ImmutableList<ProgressShow> = episodes
        .map { it.toProgressShow() }
        .toImmutableList()

    private fun Episode.toUpcomingItem() =
        HomeUpcomingItem.EpisodeItem(
            show = PreviewData.show1,
            episodes = persistentListOf(this),
            isFullSeason = false,
        )

    private fun Episode.toProgressShow() =
        ProgressShow(
            progress = ProgressShow.Progress(
                lastWatchedAt = nowUtc(),
                aired = 12,
                completed = 4,
                stats = null,
                nextEpisode = this,
                lastEpisode = null,
                isLatestAired = false,
            ),
            show = PreviewData.show1,
        )
}
