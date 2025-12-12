package tv.trakt.trakt.core.discover.sections.recommended.usecase.movies

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.recommended.data.local.movies.RecommendedMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import java.time.Instant

internal class CustomGetRecommendedMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localRecommendedSource: RecommendedMoviesLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
    private val customThemeUseCase: CustomThemeUseCase,
) : GetRecommendedMoviesUseCase {
    override suspend fun getLocalMovies(): ImmutableList<DiscoverItem.MovieItem> {
        return localRecommendedSource.getMovies()
            .toImmutableList()
            .also {
                localMovieSource.upsertMovies(
                    it.asyncMap { item -> item.movie },
                )
            }
    }

    override suspend fun getMovies(
        limit: Int,
        skipLocal: Boolean,
    ): ImmutableList<DiscoverItem.MovieItem> {
        val config = customThemeUseCase.getConfig()

        val customGenres = when {
            config.enabled -> config.theme?.filters?.movies?.recommended?.genres
            else -> null
        }
        val customSubgenres = when {
            config.enabled -> config.theme?.filters?.movies?.recommended?.subgenres
            else -> null
        }

        val customYears = when {
            config.enabled -> config.theme?.filters?.movies?.recommended?.years?.toString()
            else -> null
        }

        return remoteSource.getRecommended(
            limit = limit,
            genres = customGenres,
            subgenres = customSubgenres,
            years = customYears,
        )
            .asyncMap {
                DiscoverItem.MovieItem(
                    movie = Movie.fromDto(it),
                    count = 0,
                )
            }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localRecommendedSource.addMovies(
                        movies = movies.take(DEFAULT_SECTION_LIMIT),
                        addedAt = Instant.now(),
                    )
                }

                localMovieSource.upsertMovies(
                    movies.asyncMap { item -> item.movie },
                )
            }
    }
}
