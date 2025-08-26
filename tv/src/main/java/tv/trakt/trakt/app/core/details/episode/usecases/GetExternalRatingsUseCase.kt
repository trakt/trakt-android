package tv.trakt.trakt.app.core.details.episode.usecases

import tv.trakt.trakt.app.common.model.ExternalRating
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId

internal class GetExternalRatingsUseCase(
    private val remoteSource: EpisodesRemoteDataSource,
) {
    suspend fun getExternalRatings(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
    ): ExternalRating {
        val ratings = remoteSource.getEpisodeExternalRatings(
            showId = showId,
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
        )

        return ExternalRating(
            tmdb = ExternalRating.TmdbRating(
                rating = ratings.tmdb?.rating ?: 0F,
                votes = ratings.tmdb?.votes ?: 0,
                link = ratings.tmdb?.link,
            ),
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
