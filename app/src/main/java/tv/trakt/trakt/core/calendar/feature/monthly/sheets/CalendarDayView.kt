package tv.trakt.trakt.core.calendar.feature.monthly.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.fullDayFormat
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.calendar.model.CalendarItem.EpisodeItem
import tv.trakt.trakt.core.calendar.model.CalendarItem.MovieItem
import tv.trakt.trakt.core.calendar.ui.CalendarEpisodeItemView
import tv.trakt.trakt.core.calendar.ui.CalendarMovieItemView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate

/** Single calendar day rendered with the same item cards as the weekly calendar. */
@Composable
internal fun CalendarDayView(
    date: LocalDate,
    modifier: Modifier = Modifier,
    items: ImmutableList<CalendarItem>? = null,
    itemsLoading: ImmutableSet<TraktId>? = null,
    onEpisodeClick: (EpisodeItem) -> Unit = {},
    onShowClick: (EpisodeItem) -> Unit = {},
    onMovieClick: (MovieItem) -> Unit = {},
    onCheckClick: (CalendarItem) -> Unit = {},
    onCheckLongClick: (CalendarItem) -> Unit = {},
    onRemoveClick: (CalendarItem) -> Unit = {},
) {
    val dayFormat = fullDayFormat()
    val isToday = remember(date) { date == nowLocalDay() }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = spacedBy(6.dp),
            verticalAlignment = CenterVertically,
        ) {
            if (isToday) {
                Box(
                    modifier = Modifier
                        .background(color = Purple400, shape = RoundedCornerShape(100))
                        .size(3.dp, 16.dp),
                )
            }
            TraktHeader(title = date.format(dayFormat).capitalize())
        }

        if (items.isNullOrEmpty()) {
            Text(
                text = stringResource(R.string.text_calendar_placeholder_2),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta,
            )
            return@Column
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(TraktTheme.size.calendarGridColumns),
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
            verticalArrangement = spacedBy(TraktTheme.spacing.mainGridVerticalSpace),
            contentPadding = PaddingValues(bottom = TraktTheme.spacing.mainListVerticalSpace),
            overscrollEffect = null,
        ) {
            items(
                count = items.size,
                key = { index -> items[index].id.value },
            ) { index ->
                when (val item = items[index]) {
                    is EpisodeItem -> CalendarEpisodeItemView(
                        item = item,
                        itemLoading = itemsLoading?.contains(item.id) == true,
                        midReleases = true,
                        onClick = { onEpisodeClick(item) },
                        onShowClick = { onShowClick(item) },
                        onCheckClick = { onCheckClick(item) },
                        onCheckLongClick = { onCheckLongClick(item) },
                        onRemoveClick = { onRemoveClick(item) },
                    )

                    is MovieItem -> CalendarMovieItemView(
                        item = item,
                        itemLoading = itemsLoading?.contains(item.id) == true,
                        onClick = { onMovieClick(item) },
                        onCheckClick = { onCheckClick(item) },
                        onCheckLongClick = { onCheckLongClick(item) },
                        onRemoveClick = { onRemoveClick(item) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
private fun Preview() {
    val items = persistentListOf(
        EpisodeItem(
            show = PreviewData.show1,
            episodes = persistentListOf(PreviewData.episode1),
            watched = false,
        ),
        EpisodeItem(
            show = PreviewData.show2,
            episodes = persistentListOf(PreviewData.episode1),
            watched = true,
        ),
        MovieItem(
            movie = PreviewData.movie1,
            watched = false,
        ),
    )

    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Purple400.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            CalendarDayView(
                date = nowLocalDay(),
                items = items,
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}
