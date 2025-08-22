package tv.trakt.trakt.app.core.home.sections.social.viewall

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.home.sections.social.model.SocialActivityItem
import tv.trakt.trakt.app.core.home.sections.social.views.EpisodeSocialItemView
import tv.trakt.trakt.app.core.home.sections.social.views.MovieSocialItemView
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

@Composable
internal fun SocialViewAllScreen(
    viewModel: SocialViewAllViewModel,
    onNavigateToEpisode: (TraktId, Episode) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SocialViewAllContent(
        state = state,
        onEpisodeClick = { showId, episode ->
            onNavigateToEpisode(showId, episode)
        },
        onMovieClick = { movieId ->
            onNavigateToMovie(movieId)
        },
    )
}

@Composable
private fun SocialViewAllContent(
    state: SocialViewAllState,
    modifier: Modifier = Modifier,
    onEpisodeClick: (TraktId, Episode) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    var focusedItem by remember { mutableStateOf<SocialActivityItem?>(null) }
    var focusedItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    val focusRequesters = remember { mutableMapOf<Long, FocusRequester>() }

    LaunchedEffect(state.isLoading) {
        // Used when list is updated after user comes back and modifies history/watchlist etc.
        if (state.isLoading) {
            focusedItem = null
            focusedItemId = null
            focusRequesters.clear()
        }
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedItemId]?.requestSafeFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedItem?.images?.getFanartUrl(Size.FULL),
            saturation = 0F,
            crossfade = true,
        )

        val gridSpace = TraktTheme.spacing.mainGridSpace
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = spacedBy(gridSpace),
            verticalArrangement = spacedBy(gridSpace * 2),
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = 16.dp,
                top = 30.dp,
                bottom = TraktTheme.spacing.mainContentVerticalSpace,
            ),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.list_title_social_activity),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .focusProperties {
                            down = focusRequesters.values.firstOrNull() ?: FocusRequester.Default
                        }
                        .focusable(),
                )
            }

            if (state.isLoading && state.items.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            } else if (!state.items.isNullOrEmpty()) {
                items(
                    count = state.items.size,
                    key = { index -> state.items[index].id },
                ) { index ->
                    val item = state.items[index]
                    val focusRequester = focusRequesters.getOrPut(item.id) {
                        FocusRequester()
                    }

                    when (item) {
                        is SocialActivityItem.MovieItem -> {
                            MovieSocialItemView(
                                item = item,
                                onClick = onMovieClick,
                                onFocused = { socialItem ->
                                    focusedItem = socialItem
                                    focusedItemId = socialItem?.id
                                },
                                modifier = Modifier
                                    .focusRequester(focusRequester),
                            )
                        }
                        is SocialActivityItem.EpisodeItem -> {
                            EpisodeSocialItemView(
                                item = item,
                                onClick = onEpisodeClick,
                                onFocused = { socialItem ->
                                    focusedItem = socialItem
                                    focusedItemId = socialItem.id
                                },
                                modifier = Modifier
                                    .focusRequester(focusRequester),
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.error != null) {
        GenericErrorView(
            error = state.error,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = TraktTheme.spacing.mainContentStartSpace,
                    end = TraktTheme.spacing.mainContentEndSpace,
                ),
        )
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
    heightDp = 1000,
)
@Composable
private fun Preview() {
    TraktTheme {
        SocialViewAllContent(
            state = SocialViewAllState(),
            onEpisodeClick = { _, _ -> },
            onMovieClick = { _ -> },
        )
    }
}
