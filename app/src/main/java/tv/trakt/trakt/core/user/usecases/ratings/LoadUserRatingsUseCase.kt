package tv.trakt.trakt.core.user.usecases.ratings

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.user.data.local.ratings.UserRatingsLocalDataSource
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

internal class LoadUserRatingsUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: UserRatingsLocalDataSource,
) {
    suspend fun loadLocalShows(): ImmutableMap<TraktId, UserRating> {
        return localSource.getShows()
            .associateBy { it.mediaId }
            .toImmutableMap()
    }

    suspend fun loadLocalMovies(): ImmutableMap<TraktId, UserRating> {
        return localSource.getMovies()
            .associateBy { it.mediaId }
            .toImmutableMap()
    }

    suspend fun loadLocalEpisodes(): ImmutableMap<TraktId, UserRating> {
        return localSource.getEpisodes()
            .associateBy { it.mediaId }
            .toImmutableMap()
    }

    suspend fun isShowsLoaded(): Boolean {
        return localSource.isShowsLoaded()
    }

    suspend fun isMoviesLoaded(): Boolean {
        return localSource.isMoviesLoaded()
    }

    suspend fun isEpisodesLoaded(): Boolean {
        return localSource.isEpisodesLoaded()
    }

    suspend fun loadShows(): ImmutableMap<TraktId, UserRating> {
        return remoteSource.getRatingsShows()
            .asyncMap {
                UserRating(
                    mediaId = it.show!!.ids.trakt.toTraktId(),
                    mediaType = MediaType.SHOW,
                    rating = it.rating,
                )
            }
            .also {
                localSource.setShows(it)
            }
            .associateBy { it.mediaId }
            .toImmutableMap()
    }

    suspend fun loadMovies(): ImmutableMap<TraktId, UserRating> {
        return remoteSource.getRatingsMovies()
            .asyncMap {
                UserRating(
                    mediaId = it.movie!!.ids.trakt.toTraktId(),
                    mediaType = MediaType.MOVIE,
                    rating = it.rating,
                )
            }
            .also {
                localSource.setMovies(it)
            }
            .associateBy { it.mediaId }
            .toImmutableMap()
    }

    suspend fun loadEpisodes(): ImmutableMap<TraktId, UserRating> {
        return remoteSource.getRatingsEpisodes()
            .asyncMap {
                UserRating(
                    mediaId = it.episode!!.ids.trakt.toTraktId(),
                    mediaType = MediaType.EPISODE,
                    rating = it.rating,
                )
            }
            .also {
                localSource.setEpisodes(it)
            }
            .associateBy { it.mediaId }
            .toImmutableMap()
    }
}
