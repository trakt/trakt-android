package tv.trakt.trakt.app.core.home.sections.social

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.EpisodeSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalViewAllCard
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.app.core.home.sections.social.model.SocialActivityItem
import tv.trakt.trakt.app.core.home.sections.social.views.EpisodeSocialItemView
import tv.trakt.trakt.app.core.home.sections.social.views.MovieSocialItemView
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

@Composable
internal fun HomeSocialView(
    modifier: Modifier = Modifier,
    viewModel: HomeSocialViewModel = koinViewModel(),
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (SocialActivityItem) -> Unit = {},
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToMovie: (movieId: TraktId) -> Unit,
    onNavigateToViewAll: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeSocialContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onFocused = onFocused,
        onEpisodeClick = onNavigateToEpisode,
        onMovieClick = onNavigateToMovie,
        onViewAllClick = onNavigateToViewAll,
    )
}

@Composable
internal fun HomeSocialContent(
    state: HomeSocialState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (SocialActivityItem) -> Unit = {},
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit = { _, _ -> },
    onMovieClick: (movieId: TraktId) -> Unit = {},
    onViewAllClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.list_title_social_activity),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(headerPadding),
        )

        when {
            state.isLoading -> {
                ContentLoadingList(
                    contentPadding = contentPadding,
                )
            }

            state.items?.isEmpty() == true -> {
                Text(
                    text = stringResource(R.string.list_placeholder_empty),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier.padding(headerPadding),
                )
            }

            else -> {
                ContentList(
                    listItems = { (state.items ?: emptyList()).toImmutableList() },
                    onFocused = onFocused,
                    onEpisodeClick = onEpisodeClick,
                    onMovieClick = onMovieClick,
                    onViewAllClick = onViewAllClick,
                    contentPadding = contentPadding,
                )
            }
        }
    }
}

@Composable
private fun ContentList(
    listItems: () -> ImmutableList<SocialActivityItem>,
    onFocused: (SocialActivityItem) -> Unit,
    onEpisodeClick: (TraktId, Episode) -> Unit,
    onMovieClick: (TraktId) -> Unit = {},
    onViewAllClick: () -> Unit,
    contentPadding: PaddingValues,
    focusRequesters: Map<String, FocusRequester> = emptyMap(),
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
        modifier = Modifier.focusRequester(focusRequesters["content"] ?: FocusRequester.Default),
    ) {
        items(
            items = listItems(),
            key = { it.id },
        ) {
            when (it) {
                is SocialActivityItem.MovieItem ->
                    MovieSocialItemView(
                        item = it,
                        onClick = onMovieClick,
                        onFocused = onFocused,
                    )
                is SocialActivityItem.EpisodeItem ->
                    EpisodeSocialItemView(
                        item = it,
                        onClick = onEpisodeClick,
                        onFocused = onFocused,
                    )
            }
        }

        if (listItems().size >= HOME_SECTION_LIMIT) {
            item {
                HorizontalViewAllCard(
                    onClick = onViewAllClick,
                )
            }
        }

        emptyFocusListItems()
    }
}

@Composable
private fun ContentLoadingList(contentPadding: PaddingValues) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(count = 10) {
            EpisodeSkeletonCard()
        }
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        HomeSocialContent(
            state = HomeSocialState(),
        )
    }
}
