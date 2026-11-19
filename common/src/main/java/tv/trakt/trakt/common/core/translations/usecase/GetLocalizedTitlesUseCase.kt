package tv.trakt.trakt.common.core.translations.usecase

import android.icu.util.ULocale
import androidx.appcompat.app.AppCompatDelegate
import timber.log.Timber
import tv.trakt.trakt.common.Config.DEFAULT_LANGUAGE
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.translations.model.LocalizedTitles
import tv.trakt.trakt.common.data.remote.TranslationsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.apiLanguage
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.v3.model.V3BulkIntlRequest
import java.util.Locale

private const val MAX_IDS_PER_TYPE = 100

class GetLocalizedTitlesUseCase(
    private val remoteSource: TranslationsRemoteSource,
    private val sessionManager: SessionManager,
) {
    suspend fun getTitles(
        movieIds: Collection<TraktId> = emptyList(),
        showIds: Collection<TraktId> = emptyList(),
        episodeIds: Collection<TraktId> = emptyList(),
    ): LocalizedTitles {
        val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        if (locale.language == DEFAULT_LANGUAGE) {
            return LocalizedTitles.Empty
        }

        if (movieIds.isEmpty() && showIds.isEmpty() && episodeIds.isEmpty()) {
            return LocalizedTitles.Empty
        }

        if (!sessionManager.isAuthenticated()) {
            return LocalizedTitles.Empty
        }

        return try {
            val response = remoteSource.getBulkTitles(
                V3BulkIntlRequest(
                    language = locale.apiLanguage,
                    country = locale.apiCountry,
                    movieIds = movieIds.capped(),
                    showIds = showIds.capped(),
                    episodeIds = episodeIds.capped(),
                ),
            )
            LocalizedTitles.fromDto(response)
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            LocalizedTitles.Empty
        }
    }

    suspend fun <T> localizeMovies(
        items: List<T>,
        movie: (T) -> Movie,
        update: (T, Movie) -> T,
    ): List<T> {
        val titles = getTitles(movieIds = items.map { movie(it).ids.trakt })
        return items.map { update(it, titles.movie(movie(it))) }
    }

    suspend fun <T> localizeShows(
        items: List<T>,
        show: (T) -> Show,
        update: (T, Show) -> T,
    ): List<T> {
        val titles = getTitles(showIds = items.map { show(it).ids.trakt })
        return items.map { update(it, titles.show(show(it))) }
    }

    private fun Collection<TraktId>.capped(): List<TraktId> {
        return distinct()
            .sortedBy { it.value }
            .take(MAX_IDS_PER_TYPE)
    }

    private val Locale.apiCountry: String
        get() = country.ifBlank { ULocale.addLikelySubtags(ULocale.forLocale(this)).country }
}
