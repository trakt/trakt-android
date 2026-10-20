package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import java.util.Locale

class TranslationsLocalSource {
    private val mutex = Mutex()

    private val showStorage = mutableMapOf<Pair<TraktId, Locale>, MediaTranslation>()
    private val movieStorage = mutableMapOf<Pair<TraktId, Locale>, MediaTranslation>()
    private val episodeStorage = mutableMapOf<Triple<TraktId, SeasonEpisode, Locale>, MediaTranslation>()

    suspend fun getShowTranslation(
        showId: TraktId,
        locale: Locale,
    ): MediaTranslation? {
        return mutex.withLock {
            showStorage[showId to locale]
        }
    }

    suspend fun upsertShowTranslation(
        showId: TraktId,
        locale: Locale,
        translation: MediaTranslation,
    ) {
        mutex.withLock {
            showStorage[showId to locale] = translation
        }
    }

    suspend fun getMovieTranslation(
        movieId: TraktId,
        locale: Locale,
    ): MediaTranslation? {
        return mutex.withLock {
            movieStorage[movieId to locale]
        }
    }

    suspend fun upsertMovieTranslation(
        movieId: TraktId,
        locale: Locale,
        translation: MediaTranslation,
    ) {
        mutex.withLock {
            movieStorage[movieId to locale] = translation
        }
    }

    suspend fun getEpisodeTranslation(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        locale: Locale,
    ): MediaTranslation? {
        return mutex.withLock {
            episodeStorage[Triple(showId, seasonEpisode, locale)]
        }
    }

    suspend fun upsertEpisodeTranslation(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        locale: Locale,
        translation: MediaTranslation,
    ) {
        mutex.withLock {
            episodeStorage[Triple(showId, seasonEpisode, locale)] = translation
        }
    }
}
