@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.shows.sections.anticipated

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.thousandsFormat
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.shows.model.WatchersShow
import tv.trakt.trakt.core.shows.ui.context.sheet.ShowContextSheet
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowsAnticipatedView(
    modifier: Modifier = Modifier,
    viewModel: ShowsAnticipatedViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onShowClick: (TraktId) -> Unit = {},
    onMoreClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var contextSheet by remember { mutableStateOf<Show?>(null) }

    ShowsAnticipatedContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onMoreClick = {
            if (!state.loading.isLoading) {
                onMoreClick()
            }
        },
        onClick = { onShowClick(it.ids.trakt) },
        onLongClick = {
            if (!state.loading.isLoading) {
                contextSheet = it
            }
        },
    )

    ShowContextSheet(
        show = contextSheet,
        onDismiss = { contextSheet = null },
    )
}

@Composable
internal fun ShowsAnticipatedContent(
    state: ShowsAnticipatedState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onClick: (Show) -> Unit = {},
    onLongClick: (Show) -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding)
                .onClick(enabled = state.loading == DONE) {
                    onMoreClick()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.list_title_most_anticipated),
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        translationX = (4.9).dp.toPx()
                    },
            )
        }

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ContentLoadingList(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
                    if (state.error != null) {
                        Text(
                            text = "${stringResource(R.string.error_text_unexpected_error_short)}\n\n${state.error}",
                            color = TraktTheme.colors.textSecondary,
                            style = TraktTheme.typography.meta,
                            maxLines = 10,
                            modifier = Modifier.padding(contentPadding),
                        )
                    } else {
                        ContentList(
                            items = (state.items ?: emptyList()).toImmutableList(),
                            contentPadding = contentPadding,
                            onClick = onClick,
                            onLongClick = onLongClick,
                        )
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
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 6) {
            VerticalMediaSkeletonCard()
        }
    }
}

@Composable
private fun ContentList(
    items: ImmutableList<WatchersShow>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onClick: (Show) -> Unit,
    onLongClick: (Show) -> Unit,
) {
    val currentList = remember { mutableIntStateOf(items.hashCode()) }

    LaunchedEffect(items) {
        val hashCode = items.hashCode()
        if (currentList.intValue != hashCode) {
            currentList.intValue = hashCode
            listState.animateScrollToItem(0)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = items,
            key = { it.show.ids.trakt.value },
        ) { item ->
            ContentListItem(
                item = item,
                onClick = { onClick(item.show) },
                onLongClick = { onLongClick(item.show) },
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
            )
        }
    }
}

@Composable
private fun ContentListItem(
    item: WatchersShow,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    VerticalMediaCard(
        title = item.show.title,
        imageUrl = item.show.images?.getPosterUrl(),
        onClick = onClick,
        onLongClick = onLongClick,
        chipContent = { modifier ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(3.dp),
                modifier = modifier,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_bookmark_off),
                    contentDescription = null,
                    tint = TraktTheme.colors.chipContent,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = item.watchers.thousandsFormat(),
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        modifier = modifier,
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        ShowsAnticipatedContent(
            state = ShowsAnticipatedState(
                loading = IDLE,
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
private fun Preview2() {
    TraktTheme {
        ShowsAnticipatedContent(
            state = ShowsAnticipatedState(
                loading = LOADING,
            ),
        )
    }
}
