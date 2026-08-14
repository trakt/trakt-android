package tv.trakt.trakt.widgets.continuewatching.data

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.allowRgb565
import coil3.size.Precision
import coil3.toBitmap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import tv.trakt.trakt.common.core.home.model.UpNextItem
import tv.trakt.trakt.common.core.home.model.UpNextMovie
import tv.trakt.trakt.common.core.home.model.UpNextShow
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upnext.usecases.GetUpNextUseCase
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.widgets.WidgetIntentTarget
import tv.trakt.trakt.widgets.continuewatching.ContinueWatchingWidgetItem
import tv.trakt.trakt.widgets.continuewatching.ContinueWatchingWidgetState
import tv.trakt.trakt.widgets.continuewatching.withPendingItem
import kotlin.math.ceil

/** Bitmaps and strings are prepared here: a Glance tree can neither suspend nor read resources. */
internal class ContinueWatchingWidgetDataSource(
    private val getUpNextUseCase: GetUpNextUseCase,
    private val imageLoader: ImageLoader,
) {
    private val loadedState = mutableStateOf(ContinueWatchingWidgetState())
    private val pendingKeyState = mutableStateOf<String?>(null)

    /**
     * Snapshot state, not a plain value: Glance hands its content lambda to the session once, so
     * everything that changes after the first frame has to be read from the composition. Writes
     * from a widget action recompose any live session and reach the launcher.
     */
    val state: ContinueWatchingWidgetState
        @Composable get() = loadedState.value.withPendingItem(pendingKeyState.value)

    suspend fun refresh(
        context: Context,
        limit: Int,
    ) {
        val state = loadRemote(context = context, limit = limit)
        // A failed refresh keeps the items already on screen; only a first load can show the error.
        if (!state.error || loadedState.value.items.isEmpty()) {
            loadedState.value = state
        }
    }

    fun setPendingItem(key: String?) {
        pendingKeyState.value = key
    }

    private suspend fun loadRemote(
        context: Context,
        limit: Int,
    ): ContinueWatchingWidgetState {
        return coroutineScope {
            val items = runCatching {
                getUpNextUseCase.getUpNext(
                    page = 1,
                    limit = limit,
                    filters = null,
                    skipLocal = true,
                )
            }.getOrElse { error ->
                Timber.w(error, "Failed to load Continue Watching widget items")
                return@coroutineScope ContinueWatchingWidgetState(error = true)
            }

            val widgetItems = items
                .take(limit)
                .map { item -> async { item.toWidgetItem(context) } }
                .awaitAll()
                .toImmutableList()

            ContinueWatchingWidgetState(items = widgetItems)
        }
    }

    private suspend fun UpNextItem.toWidgetItem(context: Context): ContinueWatchingWidgetItem {
        return when (this) {
            is UpNextShow -> ContinueWatchingWidgetItem.Show(
                key = key,
                title = show.title,
                image = loadImage(
                    context = context,
                    url = show.images?.getFanartUrl()
                        ?: progress.nextEpisode?.images?.getScreenshotUrl(),
                ),
                progress = progress.remainingPercent,
                imageTarget = progress.nextEpisode.episodeTarget(showId = show.ids.trakt),
                titleTarget = WidgetIntentTarget.Show(showId = show.ids.trakt.value),
                episodeId = progress.nextEpisode?.ids?.trakt?.value,
                episodeText = progress.nextEpisode?.seasonEpisodeText(context) ?: "N/A",
                runtimeText = (
                    progress.nextEpisode?.runtime?.inWholeMinutes
                        ?: show.runtime?.inWholeMinutes
                ).durationText(),
                remainingEpisodesText = context.getString(
                    R.string.tag_text_remaining_episodes,
                    progress.remainingEpisodes,
                ),
            )

            is UpNextMovie -> ContinueWatchingWidgetItem.Movie(
                key = key,
                title = movie.title,
                image = loadImage(context = context, url = movie.images?.getFanartUrl()),
                // Mirrors `HomeUpNextMovieView`: never a full bar, so the chip keeps its shape.
                progress = (progress.progress / 100F).coerceIn(0F, MAX_MOVIE_PROGRESS),
                imageTarget = WidgetIntentTarget.Movie(movieId = movie.ids.trakt.value),
                titleTarget = WidgetIntentTarget.Movie(movieId = movie.ids.trakt.value),
                runtimeText = movie.runtime?.inWholeMinutes.durationText(),
                remainingTimeText = context.getString(
                    R.string.tag_text_remaining_duration,
                    remainingMinutes()?.durationText() ?: "N/A",
                ),
            )
        }
    }

    private suspend fun loadImage(
        context: Context,
        url: String?,
    ): Bitmap? {
        if (url.isNullOrBlank()) return null

        val request = ImageRequest.Builder(context)
            .data(url)
            .size(IMAGE_WIDTH_PX, IMAGE_HEIGHT_PX)
            .precision(Precision.INEXACT)
            .allowRgb565(true)
            .allowHardware(false)
            .build()

        return when (val result = imageLoader.execute(request)) {
            is SuccessResult -> result.image.toBitmap()
            else -> null
        }
    }

    private companion object {
        const val MAX_MOVIE_PROGRESS = 0.99F

        // Downscaled deliberately: every bitmap rides along in the RemoteViews parcel.
        const val IMAGE_WIDTH_PX = 384
        const val IMAGE_HEIGHT_PX = 216
    }
}

private fun Episode?.episodeTarget(showId: TraktId): WidgetIntentTarget {
    if (this == null) return WidgetIntentTarget.Show(showId = showId.value)

    return WidgetIntentTarget.Episode(
        showId = showId.value,
        episodeId = ids.trakt.value,
        season = season,
        number = number,
    )
}

private fun Episode.seasonEpisodeText(context: Context): String {
    val prefix = context.getString(R.string.episode_footer_season_episode, season, number)
    return when {
        title.isNotBlank() -> "$prefix - $title"
        else -> prefix
    }
}

private fun UpNextMovie.remainingMinutes(): Long? {
    val runtime = movie.runtime?.inWholeMinutes ?: return null
    return ceil((1F - progress.progress / 100F) * runtime).toLong()
}

private fun Long?.durationText(): String = this?.durationFormat() ?: "N/A"
