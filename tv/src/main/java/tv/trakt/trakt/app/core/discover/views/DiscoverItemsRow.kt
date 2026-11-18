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
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.VerticalViewAllCard
import tv.trakt.trakt.app.core.discover.model.DiscoverMediaSection
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListVerticalItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.discover.DiscoverItem

@Composable
internal fun DiscoverItemsRow(
    header: String,
    section: DiscoverMediaSection,
    items: ImmutableList<DiscoverItem>?,
    collection: UserCollectionState,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onItemFocus: (DiscoverItem) -> Unit = {},
    onItemClick: (DiscoverItem) -> Unit = {},
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
                    VerticalMediaSkeletonCard(
                        modifier = Modifier.focusProperties { canFocus = false },
                    )
                }
            } else if (!items.isNullOrEmpty()) {
                items(
                    items = items,
                    key = { item -> item.key },
                ) { item ->
                    DiscoverItemCard(
                        item = item,
                        section = section,
                        collection = collection,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.onFocusChanged {
                            if (it.hasFocus) {
                                onItemFocus(item)
                            }
                        },
                    )
                }

                item {
                    VerticalViewAllCard(
                        onClick = onViewAllClick,
                    )
                }

                emptyFocusListVerticalItems()
            }
        }
    }
}
