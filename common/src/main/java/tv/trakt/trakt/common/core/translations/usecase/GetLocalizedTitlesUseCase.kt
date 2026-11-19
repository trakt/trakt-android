package tv.trakt.trakt.common.core.translations.usecase

import android.icu.util.ULocale
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import tv.trakt.trakt.common.Config.DEFAULT_LANGUAGE
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.library.LibraryItem
import tv.trakt.trakt.common.core.today.model.FollowingActivity
import tv.trakt.trakt.common.core.today.model.TodayForYouItem
import tv.trakt.trakt.common.core.today.model.TodayMedia
import tv.trakt.trakt.common.core.translations.model.LocalizedTitles
import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.data.local.LocalizedTitlesLocalSource
import tv.trakt.trakt.common.data.remote.TranslationsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_NOT_FOUND
import tv.trakt.trakt.common.helpers.extensions.apiLanguage
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.activity.HomeActivityItem
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.profile.ProfileCommentItem
import tv.trakt.trakt.common.model.profile.ProfileRatingItem
import tv.trakt.trakt.common.networking.api.v3.model.V3BulkIntlRequest
import java.util.Locale

private const val MAX_IDS_PER_TYPE = 100

// Bounds the parallel requests one call can make for very large lists.
private const val MAX_BATCHES = 5

class GetLocalizedTitlesUseCase(
    private val remoteSource: TranslationsRemoteSource,
    private val localSource: LocalizedTitlesLocalSource,
    private val sessionManager: SessionManager,
) {
    suspend fun getTitles(
        movieIds: Collection<TraktId> = emptyList(),
        showIds: Collection<TraktId> = emptyList(),
        episodeIds: Collection<TraktId> = emptyList(),
    ): LocalizedTitles {
        val locale = translationLocale() ?: return LocalizedTitles.Empty

        if (movieIds.isEmpty() && showIds.isEmpty() && episodeIds.isEmpty()) {
            return LocalizedTitles.Empty
        }

        if (!sessionManager.isAuthenticated()) {
            return LocalizedTitles.Empty
        }

        val now = nowUtcInstant()

        val requested = mapOf(
            MediaType.Movie to movieIds.distinct(),
            MediaType.Show to showIds.distinct(),
            MediaType.Episode to episodeIds.distinct(),
        )

        val cached = localSource.getTitles(locale.cacheKey, requested, now)
        val missing = requested.mapValues { (type, ids) ->
            val cachedIds = cached[type].orEmpty()
            ids.filterNot { it in cachedIds }.capped()
        }

        if (missing.values.all { it.isEmpty() }) {
            return cached.toLocalizedTitles()
        }

        val batches = missing.mapValues { (_, ids) -> ids.chunked(MAX_IDS_PER_TYPE) }
        val batchCount = batches.values.maxOf { it.size }
        val fetched = coroutineScope {
            (0 until batchCount)
                .map { index ->
                    async {
                        fetchBatch(
                            locale = locale,
                            ids = batches.mapValues { (_, chunks) -> chunks.getOrElse(index) { emptyList() } },
                        )
                    }
                }
                .awaitAll()
        }.fold(emptyMap<MediaType, Map<TraktId, String?>>()) { merged, batch ->
            requested.keys.associateWith { merged[it].orEmpty() + batch[it].orEmpty() }
        }

        if (fetched.values.all { it.isEmpty() }) {
            return cached.toLocalizedTitles()
        }

        localSource.putTitles(locale.cacheKey, fetched, now)

        return requested.keys
            .associateWith { cached[it].orEmpty() + fetched[it].orEmpty() }
            .toLocalizedTitles()
    }

    suspend fun getMovieTranslation(movieId: TraktId): MediaTranslation? {
        return getTranslation(MediaType.Movie, movieId) { locale ->
            remoteSource.getMovieTranslations(movieId, locale.language, locale.country)
                ?.let(MediaTranslation::fromDto)
        }
    }

    suspend fun getShowTranslation(showId: TraktId): MediaTranslation? {
        return getTranslation(MediaType.Show, showId) { locale ->
            remoteSource.getShowTranslations(showId, locale.language, locale.country)
                ?.let(MediaTranslation::fromDto)
        }
    }

    suspend fun getEpisodeTranslation(
        episodeId: TraktId,
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
    ): MediaTranslation? {
        return getTranslation(MediaType.Episode, episodeId) { locale ->
            remoteSource.getEpisodeTranslations(showId, seasonEpisode, locale.language, locale.country)
                ?.let(MediaTranslation::fromDto)
        }
    }

    /** Title and overview for a details screen. Failures other than a missing translation propagate. */
    private suspend fun getTranslation(
        type: MediaType,
        id: TraktId,
        fetch: suspend (TranslationLocale) -> MediaTranslation?,
    ): MediaTranslation? {
        val locale = translationLocale() ?: return null
        val now = nowUtcInstant()

        val translation = localSource.getTranslation(locale.cacheKey, type, id, now)
            ?: fetchTranslation(locale, fetch).also {
                localSource.putTranslation(locale.cacheKey, type, id, it, now)
            }

        return translation.takeUnless { it.isEmpty }
    }

    private suspend fun fetchTranslation(
        locale: TranslationLocale,
        fetch: suspend (TranslationLocale) -> MediaTranslation?,
    ): MediaTranslation {
        return try {
            fetch(locale) ?: MediaTranslation.Empty
        } catch (error: Exception) {
            if (error.getHttpCode() != HTTP_ERROR_NOT_FOUND) throw error
            MediaTranslation.Empty
        }
    }

    /** Titles for one request, or empty when it fails so nothing gets cached for those ids. */
    private suspend fun fetchBatch(
        locale: TranslationLocale,
        ids: Map<MediaType, List<TraktId>>,
    ): Map<MediaType, Map<TraktId, String?>> {
        return try {
            val response = remoteSource.getBulkTitles(
                V3BulkIntlRequest(
                    language = locale.language,
                    country = locale.country,
                    movieIds = ids.getValue(MediaType.Movie),
                    showIds = ids.getValue(MediaType.Show),
                    episodeIds = ids.getValue(MediaType.Episode),
                ),
            )
            val titles = LocalizedTitles.fromDto(response)
            ids.mapValues { (type, typeIds) ->
                val typeTitles = titles.ofType(type)
                typeIds.associateWith { typeTitles[it] }
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            emptyMap()
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

    suspend fun <T> localizeEpisodes(
        items: List<T>,
        episode: (T) -> Episode,
        update: (T, Episode) -> T,
    ): List<T> {
        val titles = getTitles(episodeIds = items.map { episode(it).ids.trakt })
        return items.map { update(it, titles.episode(episode(it))) }
    }

    suspend fun localizeCalendarItems(items: List<CalendarItem>): List<CalendarItem> {
        return localizeItems(
            items = items,
            media = { item ->
                when (item) {
                    is CalendarItem.EpisodeItem -> MediaRefs(show = item.show, episodes = item.episodes)
                    is CalendarItem.MovieItem -> MediaRefs(movie = item.movie)
                }
            },
            localize = LocalizedTitles::calendarItem,
        )
    }

    suspend fun localizeActivityItems(items: List<HomeActivityItem>): List<HomeActivityItem> {
        return localizeItems(
            items = items,
            media = { item ->
                when (item) {
                    is HomeActivityItem.EpisodeItem -> MediaRefs(show = item.show, episodes = listOf(item.episode))
                    is HomeActivityItem.MovieItem -> MediaRefs(movie = item.movie)
                }
            },
            localize = LocalizedTitles::activityItem,
        )
    }

    suspend fun localizeRatingItems(items: List<ProfileRatingItem>): List<ProfileRatingItem> {
        return localizeItems(
            items = items,
            media = { item ->
                when (item) {
                    is ProfileRatingItem.ShowItem -> MediaRefs(show = item.show)
                    is ProfileRatingItem.MovieItem -> MediaRefs(movie = item.movie)
                    is ProfileRatingItem.EpisodeItem -> MediaRefs(show = item.show, episodes = listOf(item.episode))
                    is ProfileRatingItem.SeasonItem -> MediaRefs(show = item.show)
                }
            },
            localize = LocalizedTitles::ratingItem,
        )
    }

    suspend fun localizeCommentItems(items: List<ProfileCommentItem>): List<ProfileCommentItem> {
        return localizeItems(
            items = items,
            media = { item ->
                when (item) {
                    is ProfileCommentItem.ShowItem -> MediaRefs(show = item.show)
                    is ProfileCommentItem.MovieItem -> MediaRefs(movie = item.movie)
                    is ProfileCommentItem.EpisodeItem -> MediaRefs(show = item.show, episodes = listOf(item.episode))
                }
            },
            localize = LocalizedTitles::commentItem,
        )
    }

    suspend fun localizeLibraryItems(items: List<LibraryItem>): List<LibraryItem> {
        return localizeItems(
            items = items,
            media = { item ->
                when (item) {
                    is LibraryItem.MovieItem -> MediaRefs(movie = item.movie)
                    is LibraryItem.EpisodeItem -> MediaRefs(show = item.show, episodes = listOf(item.episode))
                }
            },
            localize = LocalizedTitles::libraryItem,
        )
    }

    suspend fun localizeTodayForYouItems(items: List<TodayForYouItem>): List<TodayForYouItem> {
        return localizeItems(
            items = items,
            media = { item ->
                when (item) {
                    is TodayForYouItem.UpNext -> MediaRefs(show = item.show, episodes = listOf(item.episode))
                    is TodayForYouItem.StartWatching -> item.media.toRefs()
                }
            },
            localize = LocalizedTitles::todayForYouItem,
        )
    }

    suspend fun localizeFollowingActivities(items: List<FollowingActivity>): List<FollowingActivity> {
        return localizeItems(
            items = items,
            media = { item ->
                when (val target = item.target) {
                    is FollowingActivity.Target.MovieTarget -> MediaRefs(movie = target.movie)
                    is FollowingActivity.Target.ShowTarget -> MediaRefs(show = target.show)
                    is FollowingActivity.Target.SeasonTarget -> MediaRefs(show = target.show)
                    is FollowingActivity.Target.EpisodeTarget -> MediaRefs(
                        show = target.show,
                        episodes = listOf(target.episode),
                    )
                }
            },
            localize = LocalizedTitles::followingActivity,
        )
    }

    private fun TodayMedia.toRefs(): MediaRefs {
        return when (this) {
            is TodayMedia.MovieMedia -> MediaRefs(movie = movie)
            is TodayMedia.ShowMedia -> MediaRefs(show = show)
        }
    }

    private suspend fun <T> localizeItems(
        items: List<T>,
        media: (T) -> MediaRefs,
        localize: (LocalizedTitles, T) -> T,
    ): List<T> {
        val refs = items.map(media)
        val titles = getTitles(
            movieIds = refs.mapNotNull { it.movie?.ids?.trakt },
            showIds = refs.mapNotNull { it.show?.ids?.trakt },
            episodeIds = refs.flatMap { ref -> ref.episodes.map { it.ids.trakt } },
        )
        return items.map { localize(titles, it) }
    }

    private class MediaRefs(
        val movie: Movie? = null,
        val show: Show? = null,
        val episodes: List<Episode> = emptyList(),
    )

    private fun Collection<TraktId>.capped(): List<TraktId> {
        return sortedBy { it.value }
            .take(MAX_IDS_PER_TYPE * MAX_BATCHES)
    }

    private fun LocalizedTitles.ofType(type: MediaType): Map<TraktId, String> {
        return when (type) {
            MediaType.Movie -> movies
            MediaType.Show -> shows
            MediaType.Episode -> episodes
            MediaType.Season -> emptyMap()
        }
    }

    private fun Map<MediaType, Map<TraktId, String?>>.toLocalizedTitles(): LocalizedTitles {
        fun titlesOf(type: MediaType): Map<TraktId, String> {
            return get(type).orEmpty()
                .mapNotNull { (id, title) -> title?.let { id to it } }
                .toMap()
        }

        return LocalizedTitles(
            movies = titlesOf(MediaType.Movie),
            shows = titlesOf(MediaType.Show),
            episodes = titlesOf(MediaType.Episode),
        )
    }

    /** The app language for API calls, or null when it is English and nothing needs translating. */
    private fun translationLocale(): TranslationLocale? {
        val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        if (locale.language == DEFAULT_LANGUAGE) return null

        return TranslationLocale(
            language = locale.apiLanguage,
            country = locale.country.ifBlank { ULocale.addLikelySubtags(ULocale.forLocale(locale)).country },
        )
    }

    private data class TranslationLocale(
        val language: String,
        val country: String,
    ) {
        val cacheKey: String
            get() = "$language-$country"
    }
}
