package tv.trakt.trakt.core.discover.sections.trending.usecases.movies

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.trending.data.local.movies.TrendingMoviesLocalSource
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.settings.usecases.SeasonalThemeUseCase

internal class DefaultGetTrendingMoviesUseCase(
    private val remoteSource: MoviesRemoteSource,
    private val localTrendingSource: TrendingMoviesLocalSource,
    private val localMovieSource: MovieLocalSource,
    private val seasonalThemeUseCase: SeasonalThemeUseCase,
) : GetTrendingMoviesUseCase {
    override suspend fun getLocalMovies(): ImmutableList<DiscoverItem.MovieItem> {
        return localTrendingSource.getMovies()
            .sortedByDescending { it.count }
            .toImmutableList()
            .also {
                localMovieSource.upsertMovies(
                    it.asyncMap { item -> item.movie },
                )
            }
    }

    override suspend fun getMovies(
        limit: Int,
        page: Int,
        skipLocal: Boolean,
        filters: GlobalFilter,
    ): ImmutableList<DiscoverItem.MovieItem> {
        return remoteSource.getTrending(
            page = page,
            limit = limit,
            theme = seasonalThemeUseCase.activeSeason()?.remoteValue,
            filters = filters,
        )
            .asyncMap {
                DiscoverItem.MovieItem(
                    movie = Movie.fromDto(it.movie),
                    count = it.watchers,
                )
            }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localTrendingSource.setMovies(
                        movies = movies.take(DEFAULT_SECTION_LIMIT),
                    )
                }

                localMovieSource.upsertMovies(
                    movies.asyncMap { item -> item.movie },
                )
            }
    }
}
