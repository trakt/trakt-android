package tv.trakt.trakt.core.settings.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.data.local.AnticipatedMoviesLocalSource
import tv.trakt.trakt.common.data.local.AnticipatedShowsLocalSource
import tv.trakt.trakt.common.data.local.PopularMoviesLocalSource
import tv.trakt.trakt.common.data.local.PopularShowsLocalSource
import tv.trakt.trakt.common.data.local.TrendingMoviesLocalSource
import tv.trakt.trakt.common.data.local.TrendingShowsLocalSource
import tv.trakt.trakt.common.model.discover.DiscoverSection
import tv.trakt.trakt.core.discover.data.local.media.DiscoverMediaLocalSource

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
