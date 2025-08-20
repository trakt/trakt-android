package tv.trakt.trakt.app.core.lists.views

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.model.CustomList
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.CustomListSkeletonCard
import tv.trakt.trakt.app.core.details.lists.CustomListCard
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.resources.R

@Composable
internal fun ListsPersonalView(
    items: ImmutableList<CustomList>?,
    isLoading: Boolean,
    focusRequesters: Map<String, FocusRequester>,
    onFocused: (CustomList?) -> Unit,
    onClick: (CustomList) -> Unit,
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainContentStartSpace,
        end = TraktTheme.spacing.mainContentEndSpace,
    )

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = Modifier
            .focusGroup()
            .focusRequester(focusRequesters.getValue("personal")),
    ) {
        Text(
            text = stringResource(tv.trakt.trakt.resources.R.string.list_title_personal_lists),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(contentPadding),
        )

        when {
            isLoading -> {
                ListsContentLoading(
                    contentPadding = contentPadding,
                )
            }

            items?.isEmpty() == true -> {
                Text(
                    text = stringResource(R.string.list_placeholder_empty),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier.padding(contentPadding),
                )
            }

            else -> {
                ContentList(
                    listItems = { items ?: emptyList<CustomList>().toImmutableList() },
                    onFocused = onFocused,
                    onClick = onClick,
                    contentPadding = contentPadding,
                )
            }
        }
    }
}

@Composable
private fun ListsContentLoading(contentPadding: PaddingValues) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(count = 5) {
            CustomListSkeletonCard(
                modifier = Modifier
                    .height(TraktTheme.size.detailsCustomListSize)
                    .aspectRatio(CardDefaults.HorizontalImageAspectRatio),
            )
        }
    }
}

@Composable
private fun ContentList(
    listItems: () -> ImmutableList<CustomList>,
    onFocused: (CustomList?) -> Unit,
    onClick: (CustomList) -> Unit,
    contentPadding: PaddingValues,
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems(),
            key = { it.ids.trakt.value },
        ) { item ->
            CustomListCard(
                list = item,
                onClick = { onClick(item) },
                modifier = Modifier
                    .height(TraktTheme.size.detailsCustomListSize)
                    .aspectRatio(CardDefaults.HorizontalImageAspectRatio)
                    .onFocusChanged {
                        if (it.isFocused) {
                            onFocused(item)
                        }
                    },
            )
        }

        emptyFocusListItems()
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun ListsMoviesWatchlistViewPreview() {
    TraktTheme {
        ListsPersonalView(
            items = null,
            isLoading = true,
            focusRequesters = mapOf("personal" to FocusRequester()),
            onFocused = {},
            onClick = {},
        )
    }
}
