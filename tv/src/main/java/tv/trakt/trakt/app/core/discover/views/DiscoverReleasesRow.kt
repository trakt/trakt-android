package tv.trakt.trakt.app.core.discover.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalViewAllCard
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.calendar.CalendarItem

@Composable
internal fun DiscoverReleasesRow(
    header: String,
    items: ImmutableList<CalendarItem>?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onItemFocus: (CalendarItem) -> Unit = {},
    onItemClick: (CalendarItem) -> Unit = {},
    onViewAllClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(start = TraktTheme.spacing.mainContentStartSpace),
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = 32.dp,
            ),
        ) {
            if (isLoading) {
                items(count = 10) {
                    HorizontalMediaSkeletonCard(
                        footer1 = true,
                        footer2 = true,
                        modifier = Modifier.focusProperties { canFocus = false },
                    )
                }
            } else if (!items.isNullOrEmpty()) {
                items(
                    items = items,
                    key = { item -> item.key },
                ) { item ->
                    DiscoverReleaseCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.onFocusChanged {
                            if (it.isFocused) {
                                onItemFocus(item)
                            }
                        },
                    )
                }

                item {
                    HorizontalViewAllCard(
                        onClick = onViewAllClick,
                    )
                }

                emptyFocusListItems()
            }
        }
    }
}
