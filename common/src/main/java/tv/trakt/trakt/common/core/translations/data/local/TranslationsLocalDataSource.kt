package tv.trakt.trakt.common.core.translations.data.local

import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import java.util.Locale

interface TranslationsLocalDataSource {
    suspend fun getShowTranslation(
        showId: TraktId,
        locale: Locale,
    ): MediaTranslation?

    suspend fun upsertShowTranslation(
        showId: TraktId,
        locale: Locale,
        translation: MediaTranslation,
    )

    suspend fun getMovieTranslation(
        movieId: TraktId,
        locale: Locale,
    ): MediaTranslation?

    suspend fun upsertMovieTranslation(
        movieId: TraktId,
        locale: Locale,
        translation: MediaTranslation,
    )

    suspend fun getEpisodeTranslation(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        locale: Locale,
    ): MediaTranslation?

    suspend fun upsertEpisodeTranslation(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        locale: Locale,
        translation: MediaTranslation,
    )
}
