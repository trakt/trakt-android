package tv.trakt.trakt.common.core.translations.usecase

import androidx.appcompat.app.AppCompatDelegate
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.core.translations.model.fromDto
import tv.trakt.trakt.common.data.local.TranslationsLocalSource
import tv.trakt.trakt.common.data.remote.TranslationsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_NOT_FOUND
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import java.util.Locale

class GetEpisodeTranslationsUseCase(
    private val remoteSource: TranslationsRemoteSource,
    private val localSource: TranslationsLocalSource,
) {
    suspend fun getEpisodeTranslations(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        locale: Locale,
    ): MediaTranslation? {
        val currentLocale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        if (currentLocale.language == Config.DEFAULT_LANGUAGE) {
            // The API returns English translations by default.
            return null
        }

        val effectiveLocale = locale.takeIf { it.language.isNotEmpty() } ?: currentLocale
        localSource.getEpisodeTranslation(showId, seasonEpisode, effectiveLocale)?.let { return it }

        try {
            val remoteResult = remoteSource.getEpisodeTranslations(
                showId = showId,
                seasonEpisode = seasonEpisode,
                locale = effectiveLocale,
            )
            return remoteResult?.let { dto ->
                MediaTranslation.fromDto(dto).also {
                    localSource.upsertEpisodeTranslation(showId, seasonEpisode, effectiveLocale, it)
                }
            }
        } catch (error: Exception) {
            if (error.getHttpCode() == HTTP_ERROR_NOT_FOUND) {
                return null
            } else {
                throw error
            }
        }
    }
}
