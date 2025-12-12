package tv.trakt.trakt.core.discover.sections.anticipated.usecases.movies

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.movies.AnticipatedMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class CustomGetAnticipatedMoviesUseCase(
    private val remoteSource: MoviesRemoteDataSource,
    private val localAnticipatedSource: AnticipatedMoviesLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
    private val customThemeUseCase: CustomThemeUseCase,
) : GetAnticipatedMoviesUseCase {
    override suspend fun getLocalMovies(): ImmutableList<DiscoverItem.MovieItem> {
        return localAnticipatedSource.getMovies()
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
            config.enabled -> filters?.movies?.anticipated?.genres
            else -> null
        }
        val customSubgenres = when {
            config.enabled -> filters?.movies?.anticipated?.subgenres
            else -> null
        }

        val customYears = when {
            config.enabled -> filters?.movies?.anticipated?.years?.toString()
            else -> null
        }

        return remoteSource.getAnticipated(
            page = page,
            limit = limit,
            genres = customGenres,
            subgenres = customSubgenres,
            years = customYears,
        )
            .asyncMap {
                DiscoverItem.MovieItem(
                    movie = Movie.fromDto(it.movie),
                    count = it.listCount,
                )
            }
            .toImmutableList()
            .also { movies ->
                if (!skipLocal) {
                    localAnticipatedSource.addMovies(
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
