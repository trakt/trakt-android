package tv.trakt.app.tv.core.profile.sections.history.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.app.tv.common.model.SyncHistoryItem
import tv.trakt.app.tv.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.app.tv.core.episodes.model.Episode
import tv.trakt.app.tv.core.episodes.model.fromDto
import tv.trakt.app.tv.core.movies.data.local.MovieLocalDataSource
import tv.trakt.app.tv.core.movies.model.Movie
import tv.trakt.app.tv.core.movies.model.fromDto
import tv.trakt.app.tv.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.app.tv.core.shows.model.Show
import tv.trakt.app.tv.core.shows.model.fromDto
import tv.trakt.app.tv.helpers.extensions.asyncMap
import tv.trakt.app.tv.helpers.extensions.toZonedDateTime

internal class GetProfileHistoryUseCase(
    private val remoteUserSource: ProfileRemoteDataSource,
    private val localMoviesSource: MovieLocalDataSource,
    private val localEpisodesSource: EpisodeLocalDataSource,
) {
    // TODO This should not be split. Use single history once API is fixed.
    suspend fun getHistory(
        page: Int = 1,
        limit: Int,
    ): ImmutableList<SyncHistoryItem> {
        return coroutineScope {
            val remoteEpisodesAsync = async {
                remoteUserSource.getUserEpisodesHistory(page, limit)
            }
            val remoteMoviesAsync = async {
                remoteUserSource.getUserMoviesHistory(page, limit)
            }

            val remoteEpisodes = remoteEpisodesAsync.await()
                .asyncMap {
                    SyncHistoryItem(
                        id = it.id,
                        watchedAt = it.watchedAt.toZonedDateTime(),
                        type = it.type.value,
                        show = Show.fromDto(it.show),
                        episode = Episode.fromDto(it.episode),
                    )
                }.also {
                    val episodes = it.asyncMap { e -> e.episode }
                    localEpisodesSource.upsertEpisodes(episodes.filterNotNull())
                }

            val remoteMovies = remoteMoviesAsync.await()
                .asyncMap {
                    SyncHistoryItem(
                        id = it.id,
                        watchedAt = it.watchedAt.toZonedDateTime(),
                        type = it.type.value,
                        movie = Movie.fromDto(it.movie),
                    )
                }.also {
                    val movies = it.asyncMap { m -> m.movie }
                    localMoviesSource.upsertMovies(movies.filterNotNull())
                }

            return@coroutineScope (remoteEpisodes + remoteMovies)
                .sortedByDescending { it.watchedAt }
                .toImmutableList()
        }
    }
}
