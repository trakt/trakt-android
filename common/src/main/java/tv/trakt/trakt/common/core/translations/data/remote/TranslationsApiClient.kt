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
            language = locale.apiLanguage,
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
            language = locale.apiLanguage,
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
            language = locale.apiLanguage,
        ).body()

        return result
            .selectForLocale(locale) { it.country }
    }

    private fun <T> List<T>.selectForLocale(
        locale: Locale,
        country: (T) -> String?,
    ): T? {
        // Nothing to disambiguate on: take the region-agnostic entry, otherwise whatever Trakt
        // ranked first, so languages without regional variants still get translated.
        if (locale.country.isBlank()) {
            return firstOrNull { country(it).isNullOrBlank() } ?: firstOrNull()
        }

        return firstOrNull {
            country(it).equals(locale.country, ignoreCase = true)
        } ?: firstOrNull {
            country(it).isNullOrBlank()
        }
    }
}

/**
 * Android's [Locale] reports the deprecated ISO 639 codes for a handful of languages (`id` is
 * reported as `in`, `he` as `iw`, `yi` as `ji`), and Trakt files both Norwegian written standards
 * under the `no` macrolanguage. Querying with the raw code returns an empty result for those.
 */
private val Locale.apiLanguage: String
    get() = when (language) {
        "in" -> "id"
        "iw" -> "he"
        "ji" -> "yi"
        "nb", "nn" -> "no"
        else -> language
    }
