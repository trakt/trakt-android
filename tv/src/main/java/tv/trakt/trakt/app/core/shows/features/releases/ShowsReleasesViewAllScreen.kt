package tv.trakt.trakt.app.core.shows.features.releases

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.chips.FinaleChip
import tv.trakt.trakt.app.common.ui.chips.InfoChip
import tv.trakt.trakt.app.common.ui.chips.PremiereChip
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

@Composable
internal fun ShowsReleasesScreen(
    viewModel: ShowsReleasesViewAllViewModel,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ShowsReleasesViewAllContent(
        state = state,
        onEpisodeClick = onNavigateToEpisode,
    )
}

@Composable
private fun ShowsReleasesViewAllContent(
    state: ShowsReleasesViewAllState,
    modifier: Modifier = Modifier,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
) {
    var focusedItem by remember { mutableStateOf<HomeUpcomingItem.EpisodeItem?>(null) }
    var focusedItemId by rememberSaveable { mutableStateOf<Int?>(null) }
    val focusRequesters = remember { mutableMapOf<Int, FocusRequester>() }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedItemId]?.requestFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedItem?.images?.getFanartUrl(Images.Size.FULL),
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
                    text = stringResource(R.string.list_title_releases),
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
                    key = { index -> state.items[index].key },
                ) { index ->
                    val item = state.items[index]
                    val focusRequester = focusRequesters.getOrPut(item.id) {
                        FocusRequester()
                    }

                    HorizontalMediaCard(
                        title = "",
                        containerImageUrl = item.images?.getFanartUrl(),
                        onClick = { onEpisodeClick(item.show.ids.trakt, item.episode) },
                        cardContent = {
                            Column(
                                verticalArrangement = spacedBy(2.dp),
                            ) {
                                when {
                                    item.episode.isPremiere() -> PremiereChip()
                                    item.episode.isFinale() -> FinaleChip()
                                }

                                InfoChip(
                                    text = item.releaseAt?.toLocal()?.relativeDateTimeString() ?: "TBA",
                                    iconPainter = when {
                                        item.episode.isReleased -> painterResource(R.drawable.ic_calendar_check)
                                        else -> painterResource(R.drawable.ic_calendar_upcoming)
                                    },
                                    containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F),
                                )
                            }
                        },
                        footerContent = {
                            Column(
                                verticalArrangement = spacedBy(1.dp),
                            ) {
                                Text(
                                    text = item.title,
                                    style = TraktTheme.typography.cardTitle,
                                    color = TraktTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                val subtitle = when {
                                    item.isFullSeason -> stringResource(
                                        R.string.text_season_number,
                                        item.episode.season,
                                    )
                                    item.episodes.size > 1 -> stringResource(
                                        R.string.episode_footer_season_episode_range,
                                        item.episode.season,
                                        item.episodes.first().number,
                                        item.episodes.last().number,
                                    )
                                    else -> item.episode.seasonEpisodeString()
                                }

                                Text(
                                    text = subtitle,
                                    style = TraktTheme.typography.cardSubtitle,
                                    color = TraktTheme.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedItem = item
                                    focusedItemId = item.id
                                }
                            },
                    )
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
        ShowsReleasesViewAllContent(
            state = ShowsReleasesViewAllState(
                isLoading = true,
            ),
            onEpisodeClick = { _, _ -> },
        )
    }
}
