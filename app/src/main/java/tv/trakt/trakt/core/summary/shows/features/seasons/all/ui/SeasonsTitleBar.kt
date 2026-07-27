@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.seasons.all.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.summary.shows.features.seasons.all.AllShowSeasonsState
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonItem
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SeasonsTitleBar(
    state: AllShowSeasonsState,
    title: String?,
    subtitle: String?,
    onSeasonClick: (SeasonItem) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val seasonsMenuVisible = remember { mutableStateOf(false) }
    val dropdownScrollState = rememberScrollState()
    val density = LocalDensity.current

    LaunchedEffect(seasonsMenuVisible.value) {
        if (seasonsMenuVisible.value) {
            val index = state.items.seasons
                .indexOfFirst { it.season.number == state.items.selectedSeason?.number }
                .coerceAtLeast(0)
            if (index > 0) {
                val itemHeightPx = with(density) { 48.dp.roundToPx() }
                dropdownScrollState.scrollTo(index * itemHeightPx)
            }
        }
    }

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer { translationX = -2.dp.toPx() },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
            modifier = Modifier.onClick { onBackClick() },
        )

        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier.onClick {
                seasonsMenuVisible.value = true
            },
        ) {
            TraktHeader(
                title = title ?: stringResource(R.string.list_title_seasons),
                subtitle = subtitle ?: "",
            )

            Box {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_all),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )

                DropdownMenu(
                    containerColor = TraktTheme.colors.dialogContainer,
                    shape = RoundedCornerShape(20.dp),
                    expanded = seasonsMenuVisible.value,
                    scrollState = dropdownScrollState,
                    onDismissRequest = {
                        seasonsMenuVisible.value = false
                    },
                ) {
                    for (season in state.items.seasons) {
                        val seasonTitle = when (season.season.number) {
                            0 -> stringResource(R.string.text_season_specials)
                            else -> stringResource(R.string.text_season_number, season.season.number)
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = seasonTitle,
                                    style = TraktTheme.typography.buttonTertiary,
                                    color = when (season.season.number) {
                                        state.items.selectedSeason?.number -> TraktTheme.colors.textPrimary
                                        else -> TraktTheme.colors.textSecondary
                                    },
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            },
                            onClick = {
                                seasonsMenuVisible.value = false
                                onSeasonClick(season)
                            },
                        )
                    }
                }
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
private fun PreviewSeasonsTitleBar() {
    TraktTheme {
        val seasons = (1..5).map { n ->
            SeasonItem(
                season = PreviewData.season1.copy(
                    ids = PreviewData.season1.ids.copy(trakt = n.toTraktId()),
                    number = n,
                ),
                isWatched = n < 3,
            )
        }.toImmutableList()

        SeasonsTitleBar(
            state = AllShowSeasonsState(
                show = PreviewData.show1,
                items = ShowSeasons(
                    seasons = seasons,
                    selectedSeason = PreviewData.season1.copy(number = 1),
                ),
            ),
            title = stringResource(R.string.text_season_number, 1),
            subtitle = PreviewData.show1.title,
            onSeasonClick = {},
            onBackClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .background(TraktTheme.colors.backgroundPrimary),
        )
    }
}
