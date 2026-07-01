package tv.trakt.trakt.core.profile.sections.social.all

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.views.HomeEmptySocialView
import tv.trakt.trakt.core.profile.sections.social.all.ui.AllSocialUserView
import tv.trakt.trakt.core.profile.sections.social.all.ui.AllSocialUserViewSkeleton
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllProfileSocialScreen(
    modifier: Modifier = Modifier,
    viewModel: AllProfileSocialViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onUserClick: (User) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AllProfileSocialContent(
        state = state,
        modifier = modifier,
        onFilterClick = viewModel::setFilter,
        onUserClick = onUserClick,
        onBackClick = onNavigateBack,
    )
}

@Composable
internal fun AllProfileSocialContent(
    state: AllProfileSocialState,
    modifier: Modifier = Modifier,
    onFilterClick: (SocialFilter) -> Unit = {},
    onUserClick: (User) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val gridState = rememberLazyGridState()

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        val contentPadding = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight * 2),
        )

        ScrollableBackdropImage(
            translation = listScrollConnection.resultOffset,
        )

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(TraktTheme.size.mainGridColumns),
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
            verticalArrangement = spacedBy(0.dp),
            contentPadding = contentPadding,
            overscrollEffect = null,
        ) {
            val items = state.items ?: EmptyImmutableList

            item(span = { GridItemSpan(maxLineSpan) }) {
                TitleBar(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .onClick { onBackClick() },
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                ContentFilters(
                    state = state,
                    onFilterClick = onFilterClick,
                )
            }

            if (state.loading.isLoading && items.isEmpty()) {
                items(count = 18) {
                    AllSocialUserViewSkeleton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = TraktTheme.spacing.mainGridHorizontalSpace),
                    )
                }
            } else {
                items(
                    items = items,
                    key = { it.ids.trakt.value },
                ) { user ->
                    AllSocialUserView(
                        user = user,
                        enabled = !state.loading.isLoading,
                        onUserClick = { onUserClick(user) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = TraktTheme.spacing.mainGridHorizontalSpace)
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }
            }

            if (state.error != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "${
                            stringResource(R.string.error_text_unexpected_error_short)
                        }\n\n${state.error}",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                        maxLines = 10,
                    )
                }
            } else if (state.loading == Done && items.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    HomeEmptySocialView(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentFilters(
    state: AllProfileSocialState,
    modifier: Modifier = Modifier,
    onFilterClick: (SocialFilter) -> Unit,
) {
    val usersCount = state.items?.size ?: 0
    FilterChipGroup(
        modifier = modifier,
        paddingVertical = PaddingValues(top = 0.dp, bottom = 20.dp),
    ) {
        for (filter in SocialFilter.entries) {
            if (filter == SocialFilter.Requests && state.requests.isNullOrEmpty()) {
                // Requests chip only surfaces when there are pending requests.
                continue
            }
            FilterChip(
                selected = state.filter == filter,
                text = stringResource(filter.displayRes),
                leadingContent = {
                    Icon(
                        painter = painterResource(filter.iconRes),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .size(
                                when (filter) {
                                    SocialFilter.Requests -> 21.dp
                                    else -> 17.dp
                                },
                            )
                            .padding(end = 2.dp),
                    )
                },
                endContent = {
                    if (state.loading == Done && state.filter == filter) {
                        Text(
                            text = " • $usersCount",
                            style = TraktTheme.typography.buttonTertiary,
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                        )
                    }
                },
                onClick = { onFilterClick(filter) },
            )
        }
    }
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -2.dp.toPx()
            },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
        )
        TraktHeader(
            title = stringResource(R.string.list_title_social),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        AllProfileSocialContent(
            state = AllProfileSocialState(),
        )
    }
}
