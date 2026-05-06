package tv.trakt.trakt.common.core.translations.usecase

import androidx.appcompat.app.AppCompatDelegate
import tv.trakt.trakt.common.core.translations.data.local.TranslationsLocalDataSource
import tv.trakt.trakt.common.core.translations.data.remote.TranslationsRemoteDataSource
import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.core.translations.model.fromDto
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_NOT_FOUND
import tv.trakt.trakt.common.helpers.extensions.getHttpErrorCode
import tv.trakt.trakt.common.model.TraktId
import java.util.Locale

private const val DEFAULT_LANGUAGE = "en"

class GetShowTranslationsUseCase(
    private val remoteSource: TranslationsRemoteDataSource,
    private val localSource: TranslationsLocalDataSource,
) {
    suspend fun getShowTranslations(
        showId: TraktId,
        locale: Locale,
    ): MediaTranslation? {
        val currentLocale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        if (currentLocale.language == DEFAULT_LANGUAGE) {
            // The API returns English translations by default.
            return null
        }

        val effectiveLocale = locale.takeIf { it.language.isNotEmpty() } ?: currentLocale
        localSource.getShowTranslation(showId, effectiveLocale)?.let { return it }

        try {
            val remoteResult = remoteSource.getShowTranslations(
                showId = showId,
                locale = effectiveLocale,
            )
            return remoteResult?.let { dto ->
                MediaTranslation.fromDto(dto).also {
                    localSource.upsertShowTranslation(showId, effectiveLocale, it)
                }
            }
        } catch (error: Exception) {
            if (error.getHttpErrorCode() == HTTP_ERROR_NOT_FOUND) {
                return null
            } else {
                throw error
            }
        }
    }
}
