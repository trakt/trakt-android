package tv.trakt.trakt.core.calendar.feature.monthly.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.rememberAppLocale
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Month.JANUARY
import java.time.YearMonth
import java.time.format.TextStyle.SHORT_STANDALONE

// Keeps the tapped month roughly centred in the strip once it opens.
private const val CENTERING_OFFSET = 2
private val SeparatorSpacing = 8.dp

@Composable
internal fun CalendarMonthSelectorView(
    anchorMonth: YearMonth,
    selectedPage: Int,
    modifier: Modifier = Modifier,
    onMonthClick: (page: Int) -> Unit = {},
) {
    val locale = rememberAppLocale()
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selectedPage - CENTERING_OFFSET).coerceAtLeast(0),
    )

    LaunchedEffect(selectedPage) {
        listState.animateScrollToItem((selectedPage - CENTERING_OFFSET).coerceAtLeast(0))
    }

    LazyRow(
        state = listState,
        horizontalArrangement = spacedBy(5.dp),
        verticalAlignment = CenterVertically,
        contentPadding = PaddingValues(
            horizontal = TraktTheme.spacing.mainPageHorizontalSpace,
        ),
        overscrollEffect = null,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(count = MONTH_PAGE_COUNT) { page ->
            val month = remember(anchorMonth, page) {
                anchorMonth.monthForPage(page)
            }

            Row(
                horizontalArrangement = spacedBy(TraktTheme.spacing.filterChipsSpace),
                verticalAlignment = CenterVertically,
            ) {
                // Year marker opens every January so a long strip stays readable.
                if (month.month == JANUARY) {
                    YearSeparator(year = month.year)
                }
                FilterChip(
                    selected = page == selectedPage,
                    text = month.month
                        .getDisplayName(SHORT_STANDALONE, locale)
                        .capitalize(),
                    onClick = { onMonthClick(page) },
                )
            }
        }
    }
}

@Composable
private fun YearSeparator(
    year: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = spacedBy(TraktTheme.spacing.filterChipsSpace),
        verticalAlignment = CenterVertically,
        modifier = modifier.padding(horizontal = SeparatorSpacing),
    ) {
        Text(
            text = year.toString(),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.meta.copy(
                fontSize = 11.sp,
                fontWeight = W700,
            ),
            maxLines = 1,
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        CalendarMonthSelectorView(
            anchorMonth = YearMonth.now(),
            selectedPage = INITIAL_MONTH_PAGE,
        )
    }
}
