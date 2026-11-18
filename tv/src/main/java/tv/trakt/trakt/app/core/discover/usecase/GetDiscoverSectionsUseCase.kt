package tv.trakt.trakt.app.core.discover.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.core.discover.DiscoverConfig.DISCOVER_SECTION_LIMIT
import tv.trakt.trakt.app.core.discover.DiscoverConfig.DISCOVER_SECTION_RELEASES_RANGE
import tv.trakt.trakt.app.core.discover.model.DiscoverMediaSection
import tv.trakt.trakt.app.core.movies.usecase.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetPopularMoviesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetReleasesMoviesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetTrendingMoviesUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetAnticipatedShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetPopularShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetReleasesShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetTrendingShowsUseCase
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaMode.Media
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.discover.DiscoverItem

internal data class DiscoverSections(
    val trending: ImmutableList<DiscoverItem>,
    val releases: ImmutableList<CalendarItem>,
    val anticipated: ImmutableList<DiscoverItem>,
    val popular: ImmutableList<DiscoverItem>,
)

internal class GetDiscoverSectionsUseCase(
    private val getMediaUseCase: GetDiscoverMediaUseCase,
    private val getTrendingShowsUseCase: GetTrendingShowsUseCase,
    private val getPopularShowsUseCase: GetPopularShowsUseCase,
    private val getAnticipatedShowsUseCase: GetAnticipatedShowsUseCase,
    private val getReleasesShowsUseCase: GetReleasesShowsUseCase,
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    private val getAnticipatedMoviesUseCase: GetAnticipatedMoviesUseCase,
    private val getReleasesMoviesUseCase: GetReleasesMoviesUseCase,
) {
    suspend fun getSections(mode: MediaMode): DiscoverSections =
        coroutineScope {
            val trendingAsync = async { getTrending(mode) }
            val releasesAsync = async { getReleases(mode) }
            val anticipatedAsync = async { getAnticipated(mode) }
            val popularAsync = async { getPopular(mode) }

            DiscoverSections(
                trending = trendingAsync.await(),
                releases = releasesAsync.await(),
                anticipated = anticipatedAsync.await(),
                popular = popularAsync.await(),
            )
        }

    private suspend fun getTrending(mode: MediaMode): ImmutableList<DiscoverItem> {
        return when (mode) {
            Media -> getMediaUseCase.getItems(DiscoverMediaSection.Trending)
            Shows -> getTrendingShowsUseCase.getTrendingShows(DISCOVER_SECTION_LIMIT)
            Movies -> getTrendingMoviesUseCase.getTrendingMovies(DISCOVER_SECTION_LIMIT)
        }
    }

    private suspend fun getAnticipated(mode: MediaMode): ImmutableList<DiscoverItem> {
        return when (mode) {
            Media -> getMediaUseCase.getItems(DiscoverMediaSection.Anticipated)
            Shows -> getAnticipatedShowsUseCase.getAnticipatedShows(DISCOVER_SECTION_LIMIT)
            Movies -> getAnticipatedMoviesUseCase.getAnticipatedMovies(DISCOVER_SECTION_LIMIT)
        }
    }

    private suspend fun getPopular(mode: MediaMode): ImmutableList<DiscoverItem> {
        return when (mode) {
            Media -> getMediaUseCase.getItems(DiscoverMediaSection.Popular)
            Shows -> getPopularShowsUseCase.getPopularShows(DISCOVER_SECTION_LIMIT)
                .map { DiscoverItem.ShowItem(show = it) }
                .toImmutableList()

            Movies -> getPopularMoviesUseCase.getPopularMovies(DISCOVER_SECTION_LIMIT)
                .map { DiscoverItem.MovieItem(movie = it) }
                .toImmutableList()
        }
    }

    private suspend fun getReleases(mode: MediaMode): ImmutableList<CalendarItem> {
        return when (mode) {
            Media -> getMediaUseCase.getReleases(
                limit = DISCOVER_SECTION_LIMIT,
                range = DISCOVER_SECTION_RELEASES_RANGE,
            )
            Shows -> getReleasesShowsUseCase.getReleases(limit = DISCOVER_SECTION_LIMIT)
            Movies -> getReleasesMoviesUseCase.getReleases(limit = DISCOVER_SECTION_LIMIT)
        }
    }
}
