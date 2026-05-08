package tv.trakt.trakt.common.core.translations.usecase

import androidx.appcompat.app.AppCompatDelegate
import tv.trakt.trakt.common.core.translations.data.local.TranslationsLocalDataSource
import tv.trakt.trakt.common.core.translations.data.remote.TranslationsRemoteDataSource
import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.core.translations.model.fromDto
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_NOT_FOUND
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.model.TraktId
import java.util.Locale

private const val DEFAULT_LANGUAGE = "en"

class GetMovieTranslationsUseCase(
    private val remoteSource: TranslationsRemoteDataSource,
    private val localSource: TranslationsLocalDataSource,
) {
    suspend fun getMovieTranslations(
        movieId: TraktId,
        locale: Locale,
    ): MediaTranslation? {
        val currentLocale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        if (currentLocale.language == DEFAULT_LANGUAGE) {
            // The API returns English translations by default.
            return null
        }

        val effectiveLocale = locale.takeIf { it.language.isNotEmpty() } ?: currentLocale
        localSource.getMovieTranslation(movieId, effectiveLocale)?.let { return it }

        try {
            val remoteResult = remoteSource.getMovieTranslations(
                movieId = movieId,
                locale = effectiveLocale,
            )
            return remoteResult?.let { dto ->
                MediaTranslation.fromDto(dto)
                    .also {
                        localSource.upsertMovieTranslation(movieId, effectiveLocale, it)
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
