package tv.trakt.trakt.core.user.usecases.progress

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.MovieProgress
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.toSlugId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.sync.model.ProgressItem
import tv.trakt.trakt.core.user.data.local.UserProgressLocalDataSource

internal class LoadUserProgressUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: UserProgressLocalDataSource,
) {
    suspend fun loadLocalShows(): ImmutableList<ProgressItem.ShowItem> {
        return localSource.getShows()
            .toImmutableList()
    }

    suspend fun loadLocalMovies(): ImmutableList<ProgressItem.MovieItem> {
        return localSource.getMovies()
            .toImmutableList()
    }

    suspend fun isShowsLoaded(): Boolean {
        return localSource.isShowsLoaded()
    }

    suspend fun isMoviesLoaded(): Boolean {
        return localSource.isMoviesLoaded()
    }

    suspend fun isLoaded(): Boolean {
        return localSource.isShowsLoaded() && localSource.isMoviesLoaded()
    }

    suspend fun loadMoviesProgress(): ImmutableList<ProgressItem.MovieItem> {
        val progress = mutableMapOf<String, List<String>>()
        var page = 1

        while (true) {
            val pageResponse = remoteSource.getWatchedMovies(Pagination(page = page, limit = 100))
            if (pageResponse.isEmpty()) {
                break
            }
            progress.putAll(pageResponse)
            page++
        }

        val response = progress
            .map { (movieId, plays) ->
                ProgressItem.MovieItem(
                    plays = plays.size,
                    movie = MovieProgress(
                        Ids(
                            trakt = movieId.toInt().toTraktId(),
                            slug = "".toSlugId(),
                        ),
                    ),
                    lastWatchedAt = plays
                        .filter {
                            // Filter out invalid dates like "0000-00-00T00:00:00Z"
                            // (leftovers from older bugged Trakt users)
                            !it.startsWith("0000")
                        }
                        .maxOf { it.toInstant() },
                )
            }

        with(localSource) {
            setMovies(response)
        }

        return response.toImmutableList()
    }

    suspend fun loadShowsProgress(): ImmutableList<ProgressItem.ShowItem> {
        val progress = mutableMapOf<String, Map<String, Map<String, List<String>>>>()
        var page = 1

        while (true) {
            val pageResponse = remoteSource.getWatchedShows(Pagination(page = page, limit = 100))
            if (pageResponse.isEmpty()) break
            progress.putAll(pageResponse)
            page++
        }

        val response = progress
            .map { entry ->
                val showId = entry.key.toInt().toTraktId()
                val seasons = entry.value.map { seasonKey ->
                    val episodes = seasonKey.value.map { episodeKey ->
                        val episodePlays = episodeKey.value
                        ProgressItem.ShowItem.Episode(
                            id = episodeKey.key.toInt().toTraktId(),
                            plays = episodePlays.size,
                            playsDistinct = if (episodePlays.isEmpty()) 0 else 1,
                            lastWatchedAt = episodePlays.maxOf { it.toInstant() },
                        )
                    }

                    val (seasonId, seasonNumber) = seasonKey.key.split("|")
                    ProgressItem.ShowItem.Season(
                        id = seasonId.toInt().toTraktId(),
                        number = seasonNumber.toInt(),
                        episodes = episodes.toImmutableList(),
                    )
                }

                ProgressItem.ShowItem(
                    showId = showId,
                    seasons = seasons.toImmutableList(),
                )
            }

        with(localSource) {
            setShows(response)
        }

        return response.toImmutableList()
    }

    suspend fun loadProgress() {
        return coroutineScope {
            val showsAsync = async { loadShowsProgress() }
            val moviesAsync = async { loadMoviesProgress() }

            showsAsync.await()
            moviesAsync.await()
        }
    }
}
