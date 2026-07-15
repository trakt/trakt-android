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
        val result = showsApi.getShowsTranslations(
            id = showId.value.toString(),
            language = locale.language,
        ).body()

        return result
            .selectForLocale(locale) { it.country }
    }

    override suspend fun getEpisodeTranslations(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        locale: Locale,
    ): EpisodeTranslationDto? {
        val result = showsApi.getShowsEpisodeTranslations(
            id = showId.value.toString(),
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
            language = locale.language,
        ).body()

        return result
            .selectForLocale(locale) { it.country }
    }

    override suspend fun getMovieTranslations(
        movieId: TraktId,
        locale: Locale,
    ): TranslationDto? {
        val result = moviesApi.getMoviesTranslations(
            id = movieId.value.toString(),
            language = locale.language,
        ).body()

        return result
            .selectForLocale(locale) { it.country }
    }

    private fun <T> List<T>.selectForLocale(
        locale: Locale,
        country: (T) -> String?,
    ): T? {
        if (locale.country.isBlank()) {
            return firstOrNull()
        }

        return firstOrNull {
            country(it).equals(locale.country, ignoreCase = true)
        } ?: firstOrNull()
    }
}
