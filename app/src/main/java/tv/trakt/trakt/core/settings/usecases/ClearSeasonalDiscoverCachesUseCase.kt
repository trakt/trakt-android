package tv.trakt.trakt.core.settings.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.core.discover.data.local.media.DiscoverMediaLocalDataSource
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.movies.AnticipatedMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.shows.AnticipatedShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.popular.data.local.movies.PopularMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.popular.data.local.shows.PopularShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.trending.data.local.movies.TrendingMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.trending.data.local.shows.TrendingShowsLocalDataSource

/**
 * Clears cached Discover sections whose content depends on the seasonal theme param.
 */
internal class ClearSeasonalDiscoverCachesUseCase(
    private val trendingMovies: TrendingMoviesLocalDataSource,
    private val trendingShows: TrendingShowsLocalDataSource,
    private val popularMovies: PopularMoviesLocalDataSource,
    private val popularShows: PopularShowsLocalDataSource,
    private val anticipatedMovies: AnticipatedMoviesLocalDataSource,
    private val anticipatedShows: AnticipatedShowsLocalDataSource,
    private val discoverMedia: DiscoverMediaLocalDataSource,
) {
    suspend fun clear() {
        coroutineScope {
            listOf(
                async { trendingMovies.clear() },
                async { trendingShows.clear() },
                async { popularMovies.clear() },
                async { popularShows.clear() },
                async { anticipatedMovies.clear() },
                async { anticipatedShows.clear() },
                async {
                    discoverMedia.clear(
                        listOf(
                            DiscoverSection.Trending,
                            DiscoverSection.Popular,
                            DiscoverSection.Anticipated,
                        ),
                    )
                },
            ).awaitAll()
        }
    }
}
