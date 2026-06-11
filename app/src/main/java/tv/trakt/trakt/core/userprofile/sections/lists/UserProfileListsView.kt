@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.userprofile.sections.lists

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.lists.sections.personal.ui.ListsFilters
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.mediacards.CustomListCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.CustomListSkeletonCard
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun UserProfileListsView(
    modifier: Modifier = Modifier,
    viewModel: UserProfileListsViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onListClick: (CustomList) -> Unit,
    onMoreClick: (PersonalListType) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    UserProfileListsContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onFilterClick = viewModel::setFilter,
        onListClick = onListClick,
        onMoreClick = {
            if (state.loading.isLoading) {
                return@UserProfileListsContent
            }
            onMoreClick(state.filter)
        },
    )
}

@Composable
internal fun UserProfileListsContent(
    state: UserProfileListsState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFilterClick: (PersonalListType) -> Unit = {},
    onListClick: (CustomList) -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    Column(
        modifier = modifier,
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.list_title_user_lists),
            chevron = !state.items.isNullOrEmpty() || state.loading != Done,
            modifier = Modifier
                .padding(headerPadding)
                .onClick(enabled = state.loading == Done && !state.items.isNullOrEmpty()) {
                    onMoreClick()
                },
        )

        ListsFilters(
            options = listOf(PersonalListType.Personal, PersonalListType.Collaborations).toImmutableList(),
            selected = state.filter,
            paddingHorizontal = headerPadding,
            paddingVertical = PaddingValues(top = 13.dp, bottom = 16.dp),
            onClick = onFilterClick,
        )

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                Idle, Loading -> {
                    ContentLoadingList(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }

                Done -> {
                    when {
                        state.error != null -> {
                            Text(
                                text = "${
                                    stringResource(
                                        R.string.error_text_unexpected_error_short,
                                    )
                                }\n\n${state.error}",
                                color = TraktTheme.colors.textSecondary,
                                style = TraktTheme.typography.meta,
                                maxLines = 10,
                                modifier = Modifier.padding(contentPadding),
                            )
                        }

                        state.items?.isEmpty() == true -> {
                            Text(
                                text = stringResource(R.string.list_placeholder_empty),
                                color = TraktTheme.colors.textSecondary,
                                style = TraktTheme.typography.heading6,
                                modifier = Modifier.padding(contentPadding),
                            )
                        }

                        else -> {
                            ContentList(
                                listItems = (state.items ?: emptyList()).toImmutableList(),
                                contentPadding = contentPadding,
                                onListClick = onListClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentLoadingList(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 5) {
            CustomListSkeletonCard(
                modifier = Modifier
                    .height(TraktTheme.size.customListCardSize)
                    .aspectRatio(HorizontalImageAspectRatio),
            )
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<CustomList>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onListClick: (CustomList) -> Unit,
) {
    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems,
            key = { it.ids.trakt.value },
        ) { list ->
            CustomListCard(
                list = list,
                descriptionVisible = true,
                modifier = Modifier
                    .height(TraktTheme.size.customListCardSize)
                    .aspectRatio(HorizontalImageAspectRatio)
                    .animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                    ),
                onClick = { onListClick(list) },
            )
        }
    }
}

// Previews

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        UserProfileListsContent(
            state = UserProfileListsState(
                loading = Idle,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewLoading() {
    TraktTheme {
        UserProfileListsContent(
            state = UserProfileListsState(
                loading = Loading,
            ),
        )
    }
}
