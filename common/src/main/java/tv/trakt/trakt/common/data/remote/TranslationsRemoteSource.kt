package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.EpisodeTranslationDto
import tv.trakt.trakt.common.networking.TranslationDto
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3BulkIntlRequest
import tv.trakt.trakt.common.networking.api.v3.model.V3BulkIntlResponse

class TranslationsRemoteSource(
    private val showsApi: ShowsApi,
    private val moviesApi: MoviesApi,
    private val v3Api: V3Api,
) {
    suspend fun getBulkTitles(request: V3BulkIntlRequest): V3BulkIntlResponse {
        return v3Api.getBulkIntl(request)
    }

    suspend fun getShowTranslations(
        showId: TraktId,
        language: String,
        country: String,
    ): TranslationDto? {
        val result = showsApi.getShowsTranslations(
            id = showId.value.toString(),
            language = language,
        ).body()

        return result
            .selectForCountry(country) { it.country }
    }

    suspend fun getEpisodeTranslations(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        language: String,
        country: String,
    ): EpisodeTranslationDto? {
        val result = showsApi.getShowsEpisodeTranslations(
            id = showId.value.toString(),
            season = seasonEpisode.season,
            episode = seasonEpisode.episode,
            language = language,
        ).body()

        return result
            .selectForCountry(country) { it.country }
    }

    suspend fun getMovieTranslations(
        movieId: TraktId,
        language: String,
        country: String,
    ): TranslationDto? {
        val result = moviesApi.getMoviesTranslations(
            id = movieId.value.toString(),
            language = language,
        ).body()

        return result
            .selectForCountry(country) { it.country }
    }

    private fun <T> List<T>.selectForCountry(
        target: String,
        country: (T) -> String?,
    ): T? {
        // Nothing to disambiguate on: take the region-agnostic entry, otherwise whatever Trakt
        // ranked first, so languages without regional variants still get translated.
        if (target.isBlank()) {
            return firstOrNull { country(it).isNullOrBlank() } ?: firstOrNull()
        }

        return firstOrNull {
            country(it).equals(target, ignoreCase = true)
        } ?: firstOrNull {
            country(it).isNullOrBlank()
        }
    }
}
