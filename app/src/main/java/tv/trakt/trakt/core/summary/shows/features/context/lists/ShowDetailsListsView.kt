@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.context.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.annotation.ExperimentalCoilApi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowDetailsListsView(
    show: Show,
    viewModel: ShowDetailsListsViewModel,
    inWatchlist: Boolean,
    modifier: Modifier = Modifier,
    onWatchlistClick: (() -> Unit)? = null,
    onAddListClick: ((TraktId) -> Unit)? = null,
    onRemoveListClick: ((TraktId) -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }
    var confirmRemoveListSheet by remember { mutableStateOf<CustomList?>(null) }

    ShowDetailsListsContent(
        show = show,
        lists = state.lists,
        inWatchlist = inWatchlist,
        onWatchlistClick = {
            if (inWatchlist) {
                confirmRemoveWatchlistSheet = true
            } else {
                onWatchlistClick?.invoke()
            }
        },
        onListClick = {
            if (viewModel.isInList(it.ids.trakt)) {
                confirmRemoveListSheet = it
            } else {
                onAddListClick?.invoke(it.ids.trakt)
            }
        },
        modifier = modifier,
    )

    ConfirmationSheet(
        active = confirmRemoveWatchlistSheet,
        onYes = {
            confirmRemoveWatchlistSheet = false
            onWatchlistClick?.invoke()
        },
        onNo = { confirmRemoveWatchlistSheet = false },
        title = stringResource(R.string.button_text_watchlist),
        message = stringResource(
            R.string.warning_prompt_remove_from_watchlist,
            show.title,
        ),
    )

    ConfirmationSheet(
        active = confirmRemoveListSheet != null,
        onYes = {
            confirmRemoveListSheet?.let {
                onRemoveListClick?.invoke(it.ids.trakt)
                confirmRemoveListSheet = null
            }
        },
        onNo = { confirmRemoveListSheet = null },
        title = stringResource(R.string.button_text_remove_from_list),
        message = stringResource(
            R.string.warning_prompt_remove_from_personal_list,
            show.title,
            confirmRemoveListSheet?.name ?: "",
        ),
    )
}

@Composable
private fun ShowDetailsListsContent(
    show: Show,
    inWatchlist: Boolean,
    lists: ImmutableList<Pair<CustomList, Boolean>>,
    modifier: Modifier = Modifier,
    onWatchlistClick: (() -> Unit)? = null,
    onListClick: ((CustomList) -> Unit)? = null,
) {
    val genresText = remember(show.genres) {
        show.genres.take(3).joinToString(" / ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Text(
            text = show.title,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading2,
            maxLines = 1,
            overflow = Ellipsis,
            autoSize = TextAutoSize.StepBased(
                maxFontSize = TraktTheme.typography.heading2.fontSize,
                minFontSize = 16.sp,
                stepSize = 2.sp,
            ),
        )

        Text(
            text = "${show.released?.year ?: show.year}  •  $genresText",
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmaller,
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier
                .padding(top = 2.dp),
        )

        Spacer(
            modifier = Modifier
                .padding(top = 22.dp)
                .background(Shade910)
                .fillMaxWidth()
                .height(1.dp),
        )

        ActionButtons(
            inWatchlist = inWatchlist,
            lists = lists,
            onWatchlistClick = onWatchlistClick,
            onListClick = onListClick,
            modifier = Modifier
                .padding(top = 12.dp),
        )
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    inWatchlist: Boolean,
    lists: ImmutableList<Pair<CustomList, Boolean>>,
    onWatchlistClick: (() -> Unit)? = null,
    onListClick: ((CustomList) -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier
            .graphicsLayer {
                translationX = -8.dp.toPx()
            },
    ) {
//        GhostButton(
//            text = stringResource(R.string.button_text_watchlist),
//            onClick = onWatchlistClick ?: {},
//            iconSize = 22.dp,
//            iconSpace = 16.dp,
//            icon = when {
//                inWatchlist -> painterResource(R.drawable.ic_minus)
//                else -> painterResource(R.drawable.ic_plus_round)
//            },
//        )

        for (list in lists) {
            GhostButton(
                text = list.first.name,
                onClick = {
                    onListClick?.invoke(list.first)
                },
                iconSize = 22.dp,
                iconSpace = 16.dp,
                icon = when {
                    list.second -> painterResource(R.drawable.ic_minus)
                    else -> painterResource(R.drawable.ic_plus_round)
                },
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        ShowDetailsListsContent(
            show = PreviewData.show1,
            inWatchlist = true,
            lists = listOf(PreviewData.customList1 to true).toImmutableList(),
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview2() {
    TraktTheme {
        ShowDetailsListsContent(
            show = PreviewData.show1,
            inWatchlist = false,
            lists = listOf(PreviewData.customList1 to false).toImmutableList(),
        )
    }
}
