package tv.trakt.trakt.common.core.translations.data.remote

import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.EpisodeTranslationDto
import tv.trakt.trakt.common.networking.TranslationDto
import java.util.Locale

interface TranslationsRemoteDataSource {
    suspend fun getShowTranslations(
        showId: TraktId,
        locale: Locale,
    ): TranslationDto?

    suspend fun getMovieTranslations(
        movieId: TraktId,
        locale: Locale,
    ): TranslationDto?

    suspend fun getEpisodeTranslations(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        locale: Locale,
    ): EpisodeTranslationDto?
}
