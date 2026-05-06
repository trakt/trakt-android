package tv.trakt.trakt.common.core.translations.data.remote

import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.EpisodeTranslationDto
import tv.trakt.trakt.common.networking.TranslationDto
import java.util.Locale

class TranslationsApiClient(
    private val showsApi: ShowsApi,
    private val moviesApi: MoviesApi,
) : TranslationsRemoteDataSource {
    override suspend fun getShowTranslations(
        showId: TraktId,
        locale: Locale,
    ): TranslationDto? {
        return showsApi.getShowsTranslations(
            id = showId.value.toString(),
            language = locale.language,
        ).body().firstOrNull()
    }

    override suspend fun getEpisodeTranslations(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        locale: Locale,
    ): EpisodeTranslationDto? {
        return showsApi.getShowsEpisodeTranslations(
            id = showId.value.toString(),
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
            language = locale.language,
        ).body().firstOrNull()
    }

    override suspend fun getMovieTranslations(
        movieId: TraktId,
        locale: Locale,
    ): TranslationDto? {
        return moviesApi.getMoviesTranslations(
            id = movieId.value.toString(),
            language = locale.language,
        ).body().firstOrNull()
    }
}
