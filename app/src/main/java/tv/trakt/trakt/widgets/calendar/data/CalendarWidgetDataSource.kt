package tv.trakt.trakt.widgets.calendar.data

import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.allowRgb565
import coil3.size.Precision
import coil3.toBitmap
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.EpisodeType
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.feature.weekly.usecases.GetCalendarItemsUseCase
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.widgets.WidgetIntentTarget
import tv.trakt.trakt.widgets.calendar.CalendarWidgetDay
import tv.trakt.trakt.widgets.calendar.CalendarWidgetItem
import tv.trakt.trakt.widgets.calendar.CalendarWidgetState
import tv.trakt.trakt.widgets.calendar.CalendarWidgetTag
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Bitmaps and strings are prepared here: a Glance tree can neither suspend nor read resources. */
internal class CalendarWidgetDataSource(
    private val getCalendarItemsUseCase: GetCalendarItemsUseCase,
    private val localShowSource: ShowLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
    private val localEpisodeSource: EpisodeLocalDataSource,
    private val imageLoader: ImageLoader,
) {
    private val loadedState = mutableStateOf(CalendarWidgetState())

    /**
     * Snapshot state, not a plain value: Glance hands its content lambda to the session once, so
     * everything that changes after the first frame has to be read from the composition.
     */
    val state: CalendarWidgetState
        @Composable get() = loadedState.value

    suspend fun refresh(
        context: Context,
        limit: Int,
    ) {
        val state = loadRemote(context = context, limit = limit)
        // A failed refresh keeps the days already on screen; only a first load can show the error.
        if (!state.error || loadedState.value.days.isEmpty()) {
            loadedState.value = state
        }
    }

    private suspend fun loadRemote(
        context: Context,
        limit: Int,
    ): CalendarWidgetState {
        return coroutineScope {
            val itemsByDay = runCatching {
                getCalendarItemsUseCase.getCalendarItems(
                    day = LocalDate.now(),
                    filters = GlobalFilter.Default,
                    type = ReleaseType.All,
                    skipProgress = true,
                )
            }.getOrElse { error ->
                Timber.w(error, "Failed to load Calendar widget items")
                return@coroutineScope CalendarWidgetState(error = true)
            }

            val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
            val dayFormat = DateTimeFormatter.ofPattern(DAY_PATTERN).withLocale(locale)
            val timeFormat = DateTimeFormatter
                .ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(locale)

            // Bitmaps ride along in the RemoteViews payload, so the week is capped as a whole.
            val today = LocalDate.now()
            var remaining = limit
            val days = itemsByDay
                .map { (date, items) ->
                    val dayItems = items.take(remaining.coerceAtLeast(0))
                    remaining -= dayItems.size

                    CalendarWidgetDay(
                        label = date.format(dayFormat),
                        isToday = date == today,
                        items = dayItems.toWidgetItems(
                            context = context,
                            timeFormat = timeFormat,
                        ),
                    ).also {
                        val showDayItems = dayItems.filterIsInstance<CalendarItem.EpisodeItem>()
                        val movieDayItems = dayItems.filterIsInstance<CalendarItem.MovieItem>()

                        localShowSource.upsertShows(showDayItems.map { it.show })
                        localMovieSource.upsertMovies(movieDayItems.map { it.movie })
                        localEpisodeSource.upsertEpisodes(showDayItems.flatMap { it.episodes })
                    }
                }
                .toImmutableList()

            CalendarWidgetState(days = days)
        }
    }

    private suspend fun List<CalendarItem>.toWidgetItems(
        context: Context,
        timeFormat: DateTimeFormatter,
    ): ImmutableList<CalendarWidgetItem> {
        return coroutineScope {
            map { item -> async { item.toWidgetItem(context = context, timeFormat = timeFormat) } }
                .awaitAll()
                .toImmutableList()
        }
    }

    private suspend fun CalendarItem.toWidgetItem(
        context: Context,
        timeFormat: DateTimeFormatter,
    ): CalendarWidgetItem {
        val timeText = releasedAt?.toLocal()?.format(timeFormat)?.capitalize()

        return when (this) {
            is CalendarItem.EpisodeItem -> CalendarWidgetItem(
                key = "episode.${show.ids.trakt.value}.${episode.ids.trakt.value}",
                title = show.title,
                subtitle = episodesSubtitle(context),
                image = loadImage(
                    context = context,
                    url = show.images?.getFanartUrl()
                        ?: episode.images?.getScreenshotUrl(),
                ),
                timeText = timeText,
                tag = episode.tag(),
                watched = watched,
                isMovie = false,
                imageTarget = WidgetIntentTarget.Episode(
                    showId = show.ids.trakt.value,
                    episodeId = episode.ids.trakt.value,
                    season = episode.season,
                    number = episode.number,
                ),
                titleTarget = WidgetIntentTarget.Show(showId = show.ids.trakt.value),
            )

            is CalendarItem.MovieItem -> CalendarWidgetItem(
                key = "movie.${movie.ids.trakt.value}",
                title = movie.title,
                subtitle = context.getString(R.string.translated_value_type_movie),
                image = loadImage(context = context, url = movie.images?.getFanartUrl()),
                timeText = timeText,
                tag = null,
                watched = watched,
                isMovie = true,
                imageTarget = WidgetIntentTarget.Movie(movieId = movie.ids.trakt.value),
                titleTarget = WidgetIntentTarget.Movie(movieId = movie.ids.trakt.value),
            )
        }
    }

    /**
     * Non-composable mirror of `Episode.isPremiere` / `isFinale` with `isLatestAired = true`,
     * matching the app's calendar (`midReleases = true`): mid-season markers count too.
     * Premiere wins over finale, same as the `when` in `CalendarEpisodeItemView`.
     */
    private fun Episode.tag(): CalendarWidgetTag? {
        return when {
            type?.isPremiere == true || type == EpisodeType.MID_SEASON_PREMIERE -> {
                CalendarWidgetTag.Premiere
            }

            type?.isFinale == true || type == EpisodeType.MID_SEASON_FINALE -> {
                CalendarWidgetTag.Finale
            }

            else -> {
                null
            }
        }
    }

    private fun CalendarItem.EpisodeItem.episodesSubtitle(context: Context): String {
        return when {
            isFullSeason -> {
                context.getString(R.string.text_season_number, episode.season)
            }

            episodes.size > 1 -> {
                context.getString(
                    R.string.episode_footer_season_episode_range,
                    episode.season,
                    episodes.first().number,
                    episodes.last().number,
                )
            }

            else -> {
                val prefix = context.getString(
                    R.string.episode_footer_season_episode,
                    episode.season,
                    episode.number,
                )
                when {
                    episode.title.isNotBlank() -> "$prefix - ${episode.title}"
                    else -> prefix
                }
            }
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
        /** Matches `fullDayFormat()` used by the calendar screen headers. */
        const val DAY_PATTERN = "EEEE, d MMM"

        // Downscaled deliberately: every bitmap rides along in the RemoteViews parcel, and the
        // whole week has to fit under the launcher's widget bitmap memory ceiling.
        const val IMAGE_WIDTH_PX = 320
        const val IMAGE_HEIGHT_PX = 180
    }
}
