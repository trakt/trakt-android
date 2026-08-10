package tv.trakt.trakt.core.calendar.feature.monthly.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberAppLocale
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.core.calendar.model.CalendarDayDisplay
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.calendar.model.CalendarItem.EpisodeItem
import tv.trakt.trakt.core.calendar.model.CalendarItem.MovieItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle.SHORT_STANDALONE
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters.nextOrSame
import java.time.temporal.TemporalAdjusters.previousOrSame

private val CellSpacing = 3.dp
private val CellPadding = 3.dp
private val CellContentSpacing = 2.dp
private val CellShape = RoundedCornerShape(8.dp)

private val DayNumberHeight = 17.dp
private val DayNumberMinWidth = 26.dp
private val DayNumberHorizontalPadding = 4.dp

private val CapsuleShape = RoundedCornerShape(100)

private val PosterShape = RoundedCornerShape(4.dp)
private val PosterSpacing = 2.dp

private val LabelSpacing = 2.dp
private val LabelShape = RoundedCornerShape(3.dp)
private val LabelHorizontalPadding = 2.5.dp
private val LabelVerticalPadding = 2.dp

private const val MAX_VISIBLE_POSTERS = 4
private const val MAX_VISIBLE_LABELS = 4
private const val POSTERS_PER_ROW = 2
private const val POSTER_ROWS = 2
private const val POSTER_PLACEHOLDER_RATIO = 0.6F
private const val OUT_OF_MONTH_ALPHA = 0.45F

private val FirstDayOfWeek = MONDAY

private val CalendarDayDisplay.maxVisibleItems: Int
    get() = when (this) {
        CalendarDayDisplay.Posters -> MAX_VISIBLE_POSTERS
        CalendarDayDisplay.Labels -> MAX_VISIBLE_LABELS
    }

@Composable
internal fun CalendarMonthView(
    month: YearMonth,
    modifier: Modifier = Modifier,
    firstDayOfWeek: DayOfWeek = FirstDayOfWeek,
    display: CalendarDayDisplay = CalendarDayDisplay.Posters,
    items: ImmutableMap<LocalDate, ImmutableList<CalendarItem>>? = null,
    onDayClick: (LocalDate) -> Unit = {},
) {
    val weekStarts = remember(month, firstDayOfWeek) {
        month.weekStarts(firstDayOfWeek)
    }
    val today = remember { nowLocalDay() }

    Column(
        verticalArrangement = spacedBy(CellSpacing),
        modifier = modifier.fillMaxSize(),
    ) {
        for (weekStart in weekStarts) {
            Row(
                horizontalArrangement = spacedBy(CellSpacing),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F),
            ) {
                for (index in 0..6) {
                    val date = remember(weekStart, index) {
                        weekStart.plusDays(index.toLong())
                    }
                    CalendarDayCell(
                        date = date,
                        isToday = date == today,
                        inMonth = YearMonth.from(date) == month,
                        display = display,
                        items = items?.get(date),
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight()
                            .onClick { onDayClick(date) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun CalendarWeekDaysView(
    modifier: Modifier = Modifier,
    firstDayOfWeek: DayOfWeek = FirstDayOfWeek,
) {
    val locale = rememberAppLocale()

    Row(
        horizontalArrangement = spacedBy(CellSpacing),
        modifier = modifier.fillMaxWidth(),
    ) {
        for (index in 0..6) {
            val day = remember(firstDayOfWeek, index) {
                firstDayOfWeek.plus(index.toLong())
            }
            Text(
                text = day.getDisplayName(SHORT_STANDALONE, locale).capitalize(),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta.copy(fontSize = 11.sp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.weight(1F),
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isToday: Boolean,
    inMonth: Boolean,
    modifier: Modifier = Modifier,
    display: CalendarDayDisplay = CalendarDayDisplay.Posters,
    items: ImmutableList<CalendarItem>? = null,
) {
    Column(
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = spacedBy(CellContentSpacing),
        modifier = modifier
            .alpha(if (inMonth) 1F else OUT_OF_MONTH_ALPHA)
            .background(
                color = TraktTheme.colors.dialogContainer,
                shape = CellShape,
            )
            .clip(CellShape)
            .padding(top = 2.dp)
            .padding(horizontal = CellPadding),
    ) {
        Box(
            contentAlignment = Center,
            modifier = Modifier
                .height(DayNumberHeight)
                .widthIn(min = DayNumberMinWidth)
                .alpha(if (inMonth) 1F else OUT_OF_MONTH_ALPHA)
                .padding(vertical = 0.66.dp)
                .background(
                    color = when {
                        isToday -> TraktTheme.colors.accent
                        else -> Color.Transparent
                    },
                    shape = CapsuleShape,
                )
                .padding(horizontal = DayNumberHorizontalPadding),
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = when {
                    isToday -> TraktTheme.colors.tagChipContentOnAccent
                    else -> TraktTheme.colors.textPrimary
                },
                style = TraktTheme.typography.meta.copy(
                    fontSize = 12.sp,
                    fontWeight = W700,
                ),
                maxLines = 1,
            )
        }

        AnimatedVisibility(
            visible = !items.isNullOrEmpty(),
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(100)),
        ) {
            Column(
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = spacedBy(CellContentSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                val visibleItems = remember(items, display) {
                    items.orEmpty().take(display.maxVisibleItems).toImmutableList()
                }

                when (display) {
                    CalendarDayDisplay.Posters -> CalendarDayPosters(
                        items = visibleItems,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1F)
                            .padding(vertical = CellContentSpacing),
                    )

                    CalendarDayDisplay.Labels -> CalendarDayLabels(
                        items = visibleItems,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1F)
                            .padding(top = CellContentSpacing),
                    )
                }

                val overflow = (items?.size ?: 0) - display.maxVisibleItems
                if (overflow > 0) {
                    Text(
                        text = "+$overflow",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta.copy(fontSize = 10.sp),
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 1.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayLabels(
    items: ImmutableList<CalendarItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(LabelSpacing),
        modifier = modifier,
    ) {
        for (item in items) {
            Text(
                text = when (item) {
                    is EpisodeItem -> item.show.title
                    is MovieItem -> item.movie.title
                },
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.meta.copy(
                    fontSize = 8.sp,
                    fontWeight = W500,
                ),
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = TraktTheme.colors.dialogContent,
                        shape = LabelShape,
                    )
                    .padding(
                        horizontal = LabelHorizontalPadding,
                        vertical = LabelVerticalPadding,
                    ),
            )
        }
    }
}

// Up to four posters across the tile width: one poster spans the full width, the
// rest pack two per row. Posters keep their vertical ratio, so a short day tile
// simply leaves space underneath.
@Composable
private fun CalendarDayPosters(
    items: ImmutableList<CalendarItem>,
    modifier: Modifier = Modifier,
) {
    val rows = remember(items) { items.chunked(POSTERS_PER_ROW) }
    val columns = if (items.size <= 1) 1 else POSTERS_PER_ROW
    // Two items already reserve both rows, so poster size doesn't jump between a
    // two, three and four item day.
    val rowSlots = if (items.size <= 1) 1 else POSTER_ROWS

    BoxWithConstraints(modifier = modifier) {
        val rowHeight = (maxHeight - PosterSpacing * (rowSlots - 1)) / rowSlots
        val columnWidth = (maxWidth - PosterSpacing * (columns - 1)) / columns
        val posterWidth = minOf(columnWidth, rowHeight * VerticalImageAspectRatio)

        Column(
            verticalArrangement = spacedBy(PosterSpacing),
            modifier = Modifier.fillMaxWidth(),
        ) {
            for (row in rows) {
                Row(
                    horizontalArrangement = spacedBy(PosterSpacing, CenterHorizontally),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    for (item in row) {
                        CalendarItemPoster(
                            item = item,
                            modifier = Modifier.width(posterWidth),
                        )
                    }
                    // Holds the column position of an odd trailing poster.
                    repeat(columns - row.size) {
                        Spacer(modifier = Modifier.width(posterWidth))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarItemPoster(
    item: CalendarItem,
    modifier: Modifier = Modifier,
) {
    val imageUrl = remember(item) { item.images?.getPosterUrl() }
    var isError by remember(imageUrl) { mutableStateOf(false) }

    Box(
        contentAlignment = Center,
        modifier = modifier
            .aspectRatio(VerticalImageAspectRatio)
            .clip(PosterShape)
            .background(TraktTheme.colors.placeholderContainer),
    ) {
        if (imageUrl == null || isError) {
            Icon(
                painter = painterResource(R.drawable.ic_trakt_logo),
                contentDescription = null,
                tint = TraktTheme.colors.placeholderContent,
                modifier = Modifier.fillMaxSize(POSTER_PLACEHOLDER_RATIO),
            )
            return@Box
        }

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = item.title,
            contentScale = Crop,
            onError = { isError = true },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

// Week rows covering the month,
// each starting on [firstDayOfWeek] and padded with the leading / trailing days of the side months.
private fun YearMonth.weekStarts(firstDayOfWeek: DayOfWeek): ImmutableList<LocalDate> {
    val first = atDay(1).with(previousOrSame(firstDayOfWeek))
    val last = atEndOfMonth().with(nextOrSame(firstDayOfWeek.plus(6)))
    val weeks = ChronoUnit.WEEKS.between(first, last.plusDays(1)).toInt()

    return List(weeks) { first.plusWeeks(it.toLong()) }.toImmutableList()
}

// Previews

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@OptIn(ExperimentalCoilApi::class)
@Composable
private fun Preview() {
    val month = YearMonth.now()
    val items = remember(month) { previewItems(month) }

    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Purple400.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column {
                CalendarWeekDaysView(firstDayOfWeek = MONDAY)
                CalendarMonthView(
                    month = month,
                    firstDayOfWeek = MONDAY,
                    items = items,
                )
            }
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun LabelsPreview() {
    val month = YearMonth.now()
    val items = remember(month) { previewItems(month) }

    TraktTheme {
        Column {
            CalendarWeekDaysView(firstDayOfWeek = MONDAY)
            CalendarMonthView(
                month = month,
                firstDayOfWeek = MONDAY,
                display = CalendarDayDisplay.Labels,
                items = items,
            )
        }
    }
}

private fun previewItems(month: YearMonth): ImmutableMap<LocalDate, ImmutableList<CalendarItem>> {
    val counts = mapOf(
        3 to 1,
        5 to 2,
        8 to 3,
        12 to 4,
        17 to 12,
    )

    return buildMap {
        counts.forEach { (day, count) ->
            put(month.atDay(day), List(count) { previewEpisodeItem() }.toImmutableList())
        }
        put(
            month.atDay(20),
            persistentListOf(previewEpisodeItem(watched = true), previewMovieItem()),
        )
        put(month.atDay(23), persistentListOf(previewMovieItem()))
    }.toImmutableMap()
}

private fun previewEpisodeItem(watched: Boolean = false): CalendarItem {
    return EpisodeItem(
        show = PreviewData.show1.copy(
            images = Images(
                poster = persistentListOf(
                    "walter-r2.trakt.tv/images/shows/000/142/611/posters/thumb/5248d0dfec.jpg.webp",
                ),
            ),
        ),
        episodes = persistentListOf(PreviewData.episode1),
        watched = watched,
    )
}

// No artwork - renders the placeholder branch.
private fun previewMovieItem(): CalendarItem {
    return MovieItem(
        movie = PreviewData.movie1.copy(images = null),
        watched = false,
    )
}
