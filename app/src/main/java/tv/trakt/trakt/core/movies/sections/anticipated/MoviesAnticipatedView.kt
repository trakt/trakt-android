package tv.trakt.trakt.core.movies.sections.anticipated

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.thousandsFormat
import tv.trakt.trakt.core.movies.model.WatchersMovie
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MoviesAnticipatedView(
    modifier: Modifier = Modifier,
    viewModel: MoviesAnticipatedViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MoviesAnticipatedContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
    )
}

@Composable
internal fun MoviesAnticipatedContent(
    state: MoviesAnticipatedState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.list_title_most_anticipated),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )
            Text(
                text = stringResource(R.string.button_text_view_all).uppercase(),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.buttonTertiary,
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
                    ContentList(
                        items = state.items ?: emptyList<WatchersMovie>().toImmutableList(),
                        contentPadding = contentPadding,
                    )
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
    items: ImmutableList<WatchersMovie>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
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
            key = { it.movie.ids.trakt.value },
        ) { item ->
            ContentListItem(
                item = item,
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
    item: WatchersMovie,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    VerticalMediaCard(
        title = item.movie.title,
        imageUrl = item.movie.images?.getPosterUrl(),
        onClick = onClick,
        chipContent = {
            InfoChip(
                text = item.watchers.thousandsFormat(),
                iconPainter = painterResource(R.drawable.ic_star),
            )
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
        MoviesAnticipatedContent(
            state = MoviesAnticipatedState(
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
        MoviesAnticipatedContent(
            state = MoviesAnticipatedState(
                loading = LOADING,
            ),
        )
    }
}
