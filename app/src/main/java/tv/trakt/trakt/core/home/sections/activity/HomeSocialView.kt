package tv.trakt.trakt.core.home.sections.activity

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.episodes.model.Episode
import tv.trakt.trakt.core.home.sections.activity.model.SocialActivityItem
import tv.trakt.trakt.core.home.sections.activity.views.EpisodeSocialItemView
import tv.trakt.trakt.core.home.sections.activity.views.MovieSocialItemView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HomeSocialView(
    modifier: Modifier = Modifier,
    viewModel: HomeSocialViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit = { _, _ -> },
    onNavigateToMovie: (movieId: TraktId) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeSocialContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onEpisodeClick = onNavigateToEpisode,
        onMovieClick = onNavigateToMovie,
    )
}

@Composable
internal fun HomeSocialContent(
    state: HomeSocialState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onEpisodeClick: (TraktId, Episode) -> Unit = { _, _ -> },
    onMovieClick: (TraktId) -> Unit = {},
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
                text = stringResource(R.string.list_title_activity),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )
            Text(
                text = stringResource(R.string.button_text_view_all),
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
                    if (state.items?.isEmpty() == true) {
                        Text(
                            text = stringResource(R.string.list_placeholder_empty),
                            color = TraktTheme.colors.textSecondary,
                            style = TraktTheme.typography.heading6,
                            modifier = Modifier.padding(headerPadding),
                        )
                    } else {
                        ContentList(
                            listItems = (state.items ?: emptyList()).toImmutableList(),
                            contentPadding = contentPadding,
                            onEpisodeClick = onEpisodeClick,
                            onMovieClick = onMovieClick,
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
            EpisodeSkeletonCard()
        }
    }
}

@Composable
private fun ContentList(
    listItems: ImmutableList<SocialActivityItem>,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onEpisodeClick: (TraktId, Episode) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    val currentList = remember { mutableIntStateOf(listItems.hashCode()) }

    LaunchedEffect(listItems) {
        val hashCode = listItems.hashCode()
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
            items = listItems,
            key = { it.id },
        ) { item ->
            when (item) {
                is SocialActivityItem.MovieItem ->
                    MovieSocialItemView(
                        item = item,
                        onClick = onMovieClick,
                        modifier = Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                    )
                is SocialActivityItem.EpisodeItem ->
                    EpisodeSocialItemView(
                        item = item,
                        onClick = onEpisodeClick,
                        modifier = Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                    )
            }
        }
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
        HomeSocialContent(
            state = HomeSocialState(
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
        HomeSocialContent(
            state = HomeSocialState(
                loading = LOADING,
            ),
        )
    }
}
