package tv.trakt.trakt.app.core.profile.sections.history.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.common.model.SyncHistoryItem
import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

internal class GetProfileHistoryUseCase(
    private val remoteUserSource: ProfileRemoteDataSource,
    private val localMoviesSource: MovieLocalDataSource,
    private val localEpisodesSource: EpisodeLocalDataSource,
) {
    suspend fun getHistory(
        page: Int = 1,
        limit: Int,
    ): ImmutableList<SyncHistoryItem> {
        return coroutineScope {
            val remoteHistory = remoteUserSource.getUserHistory(page, limit)

            return@coroutineScope remoteHistory
                .asyncMap {
                    SyncHistoryItem(
                        id = it.id,
                        watchedAt = it.watchedAt.toZonedDateTime(),
                        type = it.type.value,
                        show = it.show?.let { s -> Show.fromDto(s) },
                        episode = it.episode?.let { e -> Episode.fromDto(e) },
                        movie = it.movie?.let { m -> Movie.fromDto(m) },
                    )
                }
                .also {
                    val episodes = it.mapNotNull { item -> item.episode }
                    val movies = it.mapNotNull { item -> item.movie }

                    localEpisodesSource.upsertEpisodes(episodes)
                    localMoviesSource.upsertMovies(movies)
                }
                .sortedByDescending { it.watchedAt }
                .toImmutableList()
        }
    }
}
