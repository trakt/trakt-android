package tv.trakt.trakt.tv.core.details.movie.views.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.tv.common.model.CustomList
import tv.trakt.trakt.tv.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.tv.core.details.lists.CustomListCard
import tv.trakt.trakt.tv.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.tv.ui.theme.TraktTheme

@Composable
internal fun MovieCustomListsList(
    header: String,
    lists: () -> ImmutableList<CustomList>,
    onFocused: () -> Unit,
    onClick: (CustomList) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading4,
            modifier = Modifier.padding(
                start = TraktTheme.spacing.mainContentStartSpace,
            ),
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = TraktTheme.spacing.mainContentEndSpace,
            ),
        ) {
            items(
                items = lists(),
                key = { it.ids.trakt.value },
            ) { list ->
                CustomListCard(
                    list = list,
                    onClick = { onClick(list) },
                    modifier = Modifier
                        .height(TraktTheme.size.detailsCustomListSize)
                        .aspectRatio(CardDefaults.HorizontalImageAspectRatio)
                        .onFocusChanged {
                            if (it.isFocused) {
                                onFocused()
                            }
                        },
                )
            }

            emptyFocusListItems()
        }
    }
}
