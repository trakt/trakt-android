package tv.trakt.trakt.core.mixtape.results.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.core.mixtape.results.MixtapeResultsState
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

private const val DONE_COLUMNS = 3

private val SavedToShape = RoundedCornerShape(12.dp)
private val ChangeButtonHeight = 32.dp

private data class DoneRound(
    val number: Int,
    val items: List<MixtapeItem>,
)

@Composable
internal fun MixtapeDone(
    state: MixtapeResultsState,
    modifier: Modifier = Modifier,
    onSavedToClick: () -> Unit = {},
    onFindMoreClick: () -> Unit = {},
    onChangePicksClick: () -> Unit = {},
    onStartOverClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
) {
    val kept = state.rounds.flatten()
    val rounds = state.rounds
        .mapIndexed { index, items -> DoneRound(number = index + 1, items = items) }
        .filter { it.items.isNotEmpty() }
        .reversed()
    val isMultiRound = rounds.size > 1

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding().plus(4.dp),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().plus(20.dp),
                start = TraktTheme.spacing.mainPageHorizontalSpace,
                end = TraktTheme.spacing.mainPageHorizontalSpace,
            ),
    ) {
        DoneHeader(
            count = kept.size,
            onBackClick = onCloseClick,
        )

        Text(
            text = stringResource(
                R.string.text_mixtape_based_on,
                state.pickTitles.values.joinToString(", "),
            ),
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
        )

        if (kept.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1F)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.text_mixtape_results_empty),
                    style = TraktTheme.typography.paragraph,
                    color = TraktTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            SavedToRow(
                name = state.savedTo.displayName(stringResource(R.string.page_title_watchlist)),
                onClick = onSavedToClick,
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(DONE_COLUMNS),
                horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
                verticalArrangement = spacedBy(TraktTheme.spacing.mainGridVerticalSpace),
                overscrollEffect = null,
                modifier = Modifier.weight(1F),
            ) {
                rounds.forEachIndexed { index, round ->
                    if (isMultiRound) {
                        item(
                            key = "round-${round.number}",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            RoundLabel(number = round.number)
                        }
                    }
                    items(
                        count = round.items.size,
                        key = { round.items[it].key },
                    ) {
                        KeptPoster(
                            item = round.items[it],
                            new = isMultiRound && index == 0,
                        )
                    }
                }
            }
        }

        DoneButtons(
            canFindMore = state.slots.isNotEmpty(),
            onFindMoreClick = onFindMoreClick,
            onDoneClick = onCloseClick,
            onChangePicksClick = onChangePicksClick,
            onStartOverClick = onStartOverClick,
        )
    }
}

@Composable
private fun DoneHeader(
    count: Int,
    onBackClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight)
            .onClick(onClick = onBackClick),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
        )
        TraktHeader(
            title = stringResource(R.string.header_mixtape_done),
            subtitle = when (count) {
                1 -> stringResource(R.string.text_mixtape_count_one)
                else -> stringResource(R.string.text_mixtape_count, count)
            },
        )
    }
}

@Composable
private fun SavedToRow(
    name: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = TraktTheme.colors.shadowDynamicSmall, shape = SavedToShape)
            .background(TraktTheme.colors.panelCardContainer, SavedToShape)
            .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_bookmark_on),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = stringResource(R.string.text_mixtape_saved_to, name),
            style = TraktTheme.typography.paragraph,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1F),
        )
        PrimaryButton(
            text = stringResource(R.string.button_text_mixtape_change_target),
            textStyle = TraktTheme.typography.buttonTertiary,
            onClick = onClick,
            height = ChangeButtonHeight,
            contentPadding = 14.dp,
            border = BorderStroke(1.dp, TraktTheme.colors.chipContainer),
            containerColor = Color.Transparent,
            contentColor = TraktTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun RoundLabel(number: Int) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.text_mixtape_round, number),
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textSecondary,
        )
        Box(
            modifier = Modifier
                .weight(1F)
                .height(1.dp)
                .background(TraktTheme.colors.separator),
        )
    }
}

@Composable
private fun KeptPoster(
    item: MixtapeItem,
    new: Boolean,
) {
    Box {
        VerticalMediaCard(
            title = item.title,
            imageUrl = item.images?.getPosterUrl(),
            more = false,
            enabled = false,
        )
        if (new) {
            Text(
                text = stringResource(R.string.tag_text_new),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.textPrimaryOnAccent,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(TraktTheme.colors.accent, RoundedCornerShape(100))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun DoneButtons(
    canFindMore: Boolean,
    onFindMoreClick: () -> Unit,
    onDoneClick: () -> Unit,
    onChangePicksClick: () -> Unit,
    onStartOverClick: () -> Unit,
) {
    Column(verticalArrangement = spacedBy(12.dp)) {
        Row(horizontalArrangement = spacedBy(12.dp)) {
            PrimaryButton(
                text = stringResource(R.string.button_text_mixtape_finish),
                onClick = onDoneClick,
                containerColor = TraktTheme.colors.chipContainer,
                contentColor = TraktTheme.colors.textPrimary,
                modifier = Modifier.weight(1F),
            )
            if (canFindMore) {
                PrimaryButton(
                    text = stringResource(R.string.button_text_mixtape_show_more),
                    onClick = onFindMoreClick,
                    modifier = Modifier.weight(1F),
                )
            }
        }
        Row(horizontalArrangement = spacedBy(12.dp)) {
            PrimaryButton(
                text = stringResource(R.string.button_text_mixtape_change_picks),
                border = BorderStroke(1.dp, TraktTheme.colors.chipContainer),
                containerColor = Color.Transparent,
                contentColor = TraktTheme.colors.textPrimary,
                onClick = onChangePicksClick,
                modifier = Modifier.weight(1F),
            )
            PrimaryButton(
                text = stringResource(R.string.button_text_mixtape_start_over),
                border = BorderStroke(1.dp, TraktTheme.colors.chipContainer),
                containerColor = Color.Transparent,
                contentColor = TraktTheme.colors.textPrimary,
                onClick = onStartOverClick,
                modifier = Modifier.weight(1F),
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        MixtapeDone(
            state = MixtapeResultsState(
                loading = false,
                rounds = persistentListOf(
                    persistentListOf(
                        MixtapeItem.ShowItem(PreviewData.show1),
                        MixtapeItem.MovieItem(PreviewData.movie1),
                    ),
                    persistentListOf(MixtapeItem.MovieItem(PreviewData.movie2)),
                ),
                pickTitles = persistentMapOf(PreviewData.show2.ids.trakt to PreviewData.show2.title),
            ),
        )
    }
}
