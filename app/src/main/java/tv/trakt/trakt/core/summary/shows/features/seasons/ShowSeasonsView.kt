@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.shows.features.seasons

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.toSlugId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.shows.ui.context.sheet.ShowContextSheet
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons
import tv.trakt.trakt.core.summary.shows.features.seasons.ui.ShowEpisodesList
import tv.trakt.trakt.core.summary.shows.features.seasons.ui.ShowSeasonsList
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowSeasonsView(
    viewModel: ShowSeasonsViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var contextSheet by remember { mutableStateOf<Show?>(null) }

    ShowSeasonsContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onSeasonClick = viewModel::loadSeason,
    )

    ShowContextSheet(
        show = contextSheet,
        onDismiss = { contextSheet = null },
    )
}

@Composable
private fun ShowSeasonsContent(
    state: ShowSeasonsState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onSeasonClick: ((Season) -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        val headerSeasons =
            stringResource(R.string.list_title_seasons)

        val headerCurrentSeason = state.items.selectedSeason?.let {
            when {
                it.isSpecial -> stringResource(R.string.text_season_specials)
                else -> stringResource(R.string.text_season_number, it.number)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
            horizontalArrangement = spacedBy(5.dp),
            verticalAlignment = CenterVertically,
        ) {
            TraktHeader(
                title = headerSeasons,
                titleColor = TraktTheme.colors.textSecondary,
            )
            if (state.items.selectedSeason != null) {
                TraktHeader(
                    title = "/",
                    titleColor = TraktTheme.colors.textSecondary,
                )
                TraktHeader(
                    title = headerCurrentSeason ?: "",
                )
            }
        }

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(300),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ContentLoading(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
                    if (state.items.seasons.isEmpty()) {
                        ContentEmpty(
                            contentPadding = headerPadding,
                        )
                    } else {
                        ContentList(
                            show = state.show,
                            seasons = state.items,
                            contentPadding = contentPadding,
                            onSeasonClick = onSeasonClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentList(
    show: Show?,
    seasons: ShowSeasons,
    contentPadding: PaddingValues,
    onSeasonClick: ((Season) -> Unit)? = null,
    onEpisodeClick: ((Episode) -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(20.dp),
    ) {
        ShowSeasonsList(
            show = show,
            seasons = seasons.seasons,
            selectedSeason = seasons.selectedSeason?.number,
            onSeasonClick = onSeasonClick ?: {},
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxWidth(),
        )

        ShowEpisodesList(
            isLoading = seasons.isSeasonLoading,
            show = show,
            episodes = seasons.selectedSeasonEpisodes,
            onEpisodeClick = onEpisodeClick ?: {},
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ContentLoading(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    Column(
        verticalArrangement = spacedBy(20.dp),
    ) {
        LazyRow(
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
            contentPadding = contentPadding,
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (visible) 1F else 0F),
        ) {
            items(count = 6) {
                VerticalMediaSkeletonCard(
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
            contentPadding = contentPadding,
        ) {
            items(count = 3) {
                EpisodeSkeletonCard()
            }
        }
    }
}

@Composable
private fun ContentEmpty(contentPadding: PaddingValues) {
    Text(
        text = stringResource(R.string.list_placeholder_empty),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
        modifier = Modifier.padding(contentPadding),
    )
}

// -- Previews --

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ShowSeasonsContent(
                state = ShowSeasonsState(
                    loading = DONE,
                    items = ShowSeasons(
                        selectedSeasonEpisodes = listOf(PreviewData.episode1).toImmutableList(),
                        selectedSeason = Season(
                            ids = Ids(
                                trakt = 1.toTraktId(),
                                slug = "slug".toSlugId(),
                            ),
                            number = 2,
                            episodeCount = 12,
                            images = null,
                            firstAired = nowUtc(),
                            updatedAt = nowUtc(),
                        ),
                    ),
                ),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ShowSeasonsContent(
                state = ShowSeasonsState(
                    loading = LOADING,
                ),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview3() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ShowSeasonsContent(
                state = ShowSeasonsState(
                    loading = DONE,
                ),
            )
        }
    }
}
