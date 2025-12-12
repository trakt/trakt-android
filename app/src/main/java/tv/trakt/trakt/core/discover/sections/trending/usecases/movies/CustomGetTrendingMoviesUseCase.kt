package tv.trakt.trakt.core.discover.sections.trending.usecases.movies

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.trending.data.local.movies.TrendingMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class CustomGetTrendingMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localTrendingSource: TrendingMoviesLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
    private val customThemeUseCase: CustomThemeUseCase,
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
    ): ImmutableList<DiscoverItem.MovieItem> {
        val config = customThemeUseCase.getConfig()
        val filters = config.theme?.filters

        val customGenres = when {
            config.enabled -> filters?.movies?.trending?.genres
            else -> null
        }
        val customSubgenres = when {
            config.enabled -> filters?.movies?.trending?.subgenres
            else -> null
        }

        val customYears = when {
            config.enabled -> filters?.movies?.trending?.years?.toString()
            else -> null
        }

        return remoteSource.getTrending(
            page = page,
            limit = limit,
            genres = customGenres,
            subgenres = customSubgenres,
            years = customYears,
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
                    localTrendingSource.addMovies(
                        movies = movies.take(DEFAULT_SECTION_LIMIT),
                        addedAt = nowUtcInstant(),
                    )
                }

                localMovieSource.upsertMovies(
                    movies.asyncMap { item -> item.movie },
                )
            }
    }
}
