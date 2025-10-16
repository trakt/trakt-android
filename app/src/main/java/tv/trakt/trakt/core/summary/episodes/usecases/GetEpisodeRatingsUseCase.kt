package tv.trakt.trakt.core.summary.episodes.usecases

import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource

internal class GetEpisodeRatingsUseCase(
    private val remoteSource: EpisodesRemoteDataSource,
) {
    suspend fun getExternalRatings(
        showId: TraktId,
        season: Int,
        episode: Int,
    ): ExternalRating {
        val ratings = remoteSource.getExternalRatings(showId, season, episode)
        return ExternalRating(
            imdb = ExternalRating.ImdbRating(
                rating = ratings.imdb?.rating ?: 0F,
                votes = ratings.imdb?.votes ?: 0,
                link = ratings.imdb?.link,
            ),
            meta = ExternalRating.MetaRating(
                rating = ratings.metascore.rating ?: 0,
                link = ratings.metascore.link,
            ),
            rotten = ExternalRating.RottenRating(
                rating = ratings.rottenTomatoes?.rating ?: 0F,
                state = ratings.rottenTomatoes?.state,
                userRating = ratings.rottenTomatoes?.userRating,
                userState = ratings.rottenTomatoes?.userState,
                link = ratings.rottenTomatoes?.link,
            ),
        )
    }
}
