package tv.trakt.trakt.core.settings.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.core.discover.data.local.media.DiscoverMediaLocalSource
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.movies.AnticipatedMoviesLocalSource
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.shows.AnticipatedShowsLocalSource
import tv.trakt.trakt.core.discover.sections.popular.data.local.movies.PopularMoviesLocalSource
import tv.trakt.trakt.core.discover.sections.popular.data.local.shows.PopularShowsLocalSource
import tv.trakt.trakt.core.discover.sections.trending.data.local.movies.TrendingMoviesLocalSource
import tv.trakt.trakt.core.discover.sections.trending.data.local.shows.TrendingShowsLocalSource

/**
 * Clears cached Discover sections whose content depends on the seasonal theme param.
 */
internal class ClearSeasonalDiscoverCachesUseCase(
    private val trendingMovies: TrendingMoviesLocalSource,
    private val trendingShows: TrendingShowsLocalSource,
    private val popularMovies: PopularMoviesLocalSource,
    private val popularShows: PopularShowsLocalSource,
    private val anticipatedMovies: AnticipatedMoviesLocalSource,
    private val anticipatedShows: AnticipatedShowsLocalSource,
    private val discoverMedia: DiscoverMediaLocalSource,
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
