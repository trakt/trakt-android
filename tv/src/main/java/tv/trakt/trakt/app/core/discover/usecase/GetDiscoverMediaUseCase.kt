package tv.trakt.trakt.app.core.discover.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.core.discover.DiscoverConfig.DISCOVER_SECTION_LIMIT
import tv.trakt.trakt.app.core.discover.model.DiscoverMediaSection
import tv.trakt.trakt.app.core.movies.usecase.GetReleasesMoviesUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetReleasesShowsUseCase
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.MediaRemoteSource
import tv.trakt.trakt.common.helpers.extensions.popularYear
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.discover.DiscoverItem
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter

internal class GetDiscoverMediaUseCase(
    private val remoteSource: MediaRemoteSource,
    private val localShowSource: ShowLocalSource,
    private val localMovieSource: MovieLocalSource,
    private val getReleasesShowsUseCase: GetReleasesShowsUseCase,
    private val getReleasesMoviesUseCase: GetReleasesMoviesUseCase,
) {
    suspend fun getItems(
        section: DiscoverMediaSection,
        limit: Int = DISCOVER_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<DiscoverItem> {
        val items = when (section) {
            DiscoverMediaSection.Trending -> remoteSource.getTrending(
                page = page,
                limit = limit,
                filters = GlobalFilter.Default,
            ).mapNotNull { DiscoverItem.fromDto(it) }

            DiscoverMediaSection.Popular -> remoteSource.getPopular(
                page = page,
                limit = limit,
                filters = GlobalFilter.Default.copy(years = popularYear().let { it to it }),
            ).mapIndexed { index, dto -> DiscoverItem.fromDto(dto = dto, rank = index + 1) }

            DiscoverMediaSection.Anticipated -> remoteSource.getAnticipated(
                page = page,
                limit = limit,
                filters = GlobalFilter.Default,
            ).mapNotNull { DiscoverItem.fromDto(it) }
        }

        return items
            .toImmutableList()
            .also { upsertMedia(it) }
    }

    suspend fun getReleases(
        limit: Int,
        range: Int,
    ): ImmutableList<CalendarItem> =
        coroutineScope {
            val episodesAsync = async { getReleasesShowsUseCase.getReleases(limit = limit, range = range) }
            val moviesAsync = async { getReleasesMoviesUseCase.getReleases(limit = limit, range = range) }

            (episodesAsync.await() + moviesAsync.await())
                .sortedBy { it.releasedAt }
                .take(limit)
                .toImmutableList()
        }

    private suspend fun upsertMedia(items: List<DiscoverItem>) {
        localShowSource.upsertShows(
            items.filterIsInstance<DiscoverItem.ShowItem>().map { it.show },
        )
        localMovieSource.upsertMovies(
            items.filterIsInstance<DiscoverItem.MovieItem>().map { it.movie },
        )
    }
}
