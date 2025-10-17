@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.shows.features.seasons.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowSeasonsList(
    show: Show?,
    seasons: ImmutableList<Season>,
    selectedSeason: Int?,
    onSeasonClick: (Season) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.75F,
            behindFraction = 0.75F,
        ),
    )

    var initialScrolled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!initialScrolled) {
            initialScrolled = true
            val index = seasons
                .indexOfFirst { it.number == selectedSeason }
                .coerceAtLeast(0)
            listState.animateScrollToItem(index)
        }
    }

    LazyRow(
        state = listState,
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = modifier,
    ) {
        items(
            items = seasons,
            key = { item -> item.ids.trakt.value },
        ) { item ->
            val animatedAlpha: Float by animateFloatAsState(
                if (item.number == selectedSeason) 1f else 0.33f,
                animationSpec = tween(200),
                label = "alpha",
            )

            val seasonPosterUrl = item.images?.getPosterUrl()
            val showPosterUrl = show?.images?.getPosterUrl()

            VerticalMediaCard(
                title = "",
                imageUrl = seasonPosterUrl ?: showPosterUrl,
                onClick = { onSeasonClick(item) },
                chipContent = {
                    Column(
                        verticalArrangement = spacedBy(1.dp),
                    ) {
                        val seasonTitle = when {
                            item.isSpecial -> stringResource(R.string.text_season_specials)
                            else -> stringResource(R.string.text_season_number, item.number)
                        }

                        Text(
                            text = seasonTitle,
                            style = TraktTheme.typography.cardTitle,
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        item.episodeCount?.let {
                            Text(
                                text = stringResource(R.string.tag_text_number_of_episodes, it),
                                style = TraktTheme.typography.cardSubtitle,
                                color = TraktTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                modifier = Modifier.alpha(animatedAlpha),
            )
        }
    }
}
