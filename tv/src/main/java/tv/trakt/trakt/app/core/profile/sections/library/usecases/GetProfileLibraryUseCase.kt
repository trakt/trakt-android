package tv.trakt.trakt.app.core.profile.sections.library.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.core.profile.sections.library.model.LibraryItem
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

internal class GetProfileLibraryUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localMoviesSource: MovieLocalDataSource,
    private val localEpisodesSource: EpisodeLocalDataSource,
) {
    suspend fun getLibrary(
        page: Int = 1,
        limit: Int,
        availableOn: String?,
    ): ImmutableList<LibraryItem> {
        return coroutineScope {
            val items = remoteSource.getLibrary(
                extended = "full,images,colors,available_on",
                availableOn = availableOn,
                page = page,
                limit = limit,
            ).filter {
                it.movie != null ||
                    (it.episode != null && it.show != null)
            }.asyncMap { item ->
                item.movie?.let {
                    LibraryItem.MovieItem(
                        movie = Movie.Companion.fromDto(it),
                        collectedAt = item.collectedAt.toInstant(),
                        updatedAt = item.updatedAt.toInstant(),
                        availableOn = item.availableOn
                            ?.map { source -> source.name }
                            ?.toImmutableList()
                            ?: EmptyImmutableList,
                    )
                } ?: item.episode?.let {
                    LibraryItem.EpisodeItem(
                        episode = Episode.Companion.fromDto(it),
                        show = Show.Companion.fromDto(item.show!!),
                        collectedAt = item.collectedAt.toInstant(),
                        updatedAt = item.updatedAt.toInstant(),
                        availableOn = item.availableOn
                            ?.map { source -> source.name }
                            ?.toImmutableList()
                            ?: EmptyImmutableList,
                    )
                } ?: throw Error("Unknown library item type")
            }

            val asyncMovies = async {
                localMoviesSource.upsertMovies(
                    items.filterIsInstance<LibraryItem.MovieItem>()
                        .map { it.movie }
                        .toList(),
                )
            }

            val asyncEpisodes = async {
                localEpisodesSource.upsertEpisodes(
                    items.filterIsInstance<LibraryItem.EpisodeItem>()
                        .map { it.episode }
                        .toList(),
                )
            }

            awaitAll(asyncMovies, asyncEpisodes)
            items.toImmutableList()
        }
    }
}
