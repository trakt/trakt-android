package tv.trakt.trakt.common.core.translations.usecase

import androidx.appcompat.app.AppCompatDelegate
import tv.trakt.trakt.common.core.translations.data.remote.TranslationsRemoteDataSource
import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.core.translations.model.fromDto
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_NOT_FOUND
import tv.trakt.trakt.common.helpers.extensions.getHttpErrorCode
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import java.util.Locale

private const val DEFAULT_LANGUAGE = "en"

class GetEpisodeTranslationsUseCase(
    private val remoteSource: TranslationsRemoteDataSource,
) {
    suspend fun getEpisodeTranslations(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        locale: Locale,
    ): MediaTranslation? {
        val currentLocale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        if (currentLocale.language == DEFAULT_LANGUAGE) {
            // The API returns English translations by default.
            return null
        }

        try {
            // Try to fetch translations for the current locale first.
            val remoteResult = remoteSource.getEpisodeTranslations(
                showId = showId,
                seasonEpisode = seasonEpisode,
                locale = locale.takeIf { it.language.isNotEmpty() } ?: currentLocale,
            )
            return remoteResult?.let { MediaTranslation.fromDto(it) }
        } catch (error: Exception) {
            if (error.getHttpErrorCode() == HTTP_ERROR_NOT_FOUND) {
                return null
            } else {
                throw error
            }
        }
    }
}
