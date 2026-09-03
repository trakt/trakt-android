@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.context.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.annotation.ExperimentalCoilApi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomListMinimal
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.lists.ListButton
import tv.trakt.trakt.ui.components.buttons.lists.WatchlistButton
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowDetailsListsView(
    show: Show,
    viewModel: ShowDetailsListsViewModel,
    inWatchlist: Boolean,
    modifier: Modifier = Modifier,
    onWatchlistClick: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }

    ShowDetailsListsContent(
        show = show,
        loading = state.loading,
        lists = state.lists,
        showLists = state.showLists,
        toggling = state.toggling,
        inWatchlist = inWatchlist,
        onWatchlistClick = {
            if (inWatchlist) {
                confirmRemoveWatchlistSheet = true
            } else {
                onWatchlistClick?.invoke()
            }
        },
        onListClick = viewModel::toggleList,
        modifier = modifier,
    )

    RemoveConfirmationSheet(
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
}

@Composable
private fun ShowDetailsListsContent(
    show: Show,
    loading: LoadingState,
    inWatchlist: Boolean,
    lists: ImmutableList<CustomListMinimal>,
    showLists: ImmutableSet<TraktId>,
    toggling: ImmutableSet<TraktId>,
    modifier: Modifier = Modifier,
    onWatchlistClick: (() -> Unit)? = null,
    onListClick: ((CustomListMinimal) -> Unit)? = null,
) {
    val genresText = show.genres.take(2)
        .map { stringResource(it.displayStringRes) }
        .joinToString(", ")

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

        val yearText = remember(show.releasedAt) {
            show.releasedAt?.toLocal()?.year ?: show.year
        }

        Text(
            text = "$yearText  •  $genresText",
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
                .background(TraktTheme.colors.separator)
                .fillMaxWidth()
                .height(1.dp),
        )

        ActionButtons(
            loading = loading.isLoading,
            inWatchlist = inWatchlist,
            lists = lists,
            showLists = showLists,
            toggling = toggling,
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
    loading: Boolean,
    inWatchlist: Boolean,
    lists: ImmutableList<CustomListMinimal>,
    showLists: ImmutableSet<TraktId>,
    toggling: ImmutableSet<TraktId>,
    onWatchlistClick: (() -> Unit)? = null,
    onListClick: ((CustomListMinimal) -> Unit)? = null,
) {
    val scrollState = rememberScrollState()
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier
            .verticalScroll(
                state = scrollState,
                overscrollEffect = null,
            ),
    ) {
        WatchlistButton(
            checked = inWatchlist,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = -4.dp.toPx()
                }
                .onClick {
                    onWatchlistClick?.invoke()
                },
        )

        if (lists.isNotEmpty() || loading) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(6.dp),
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.list_title_personal_lists),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading5.copy(
                        fontSize = 16.sp,
                    ),
                    maxLines = 1,
                    overflow = Ellipsis,
                    textAlign = TextAlign.Start,
                    modifier = Modifier,
                )
            }
        }

        for (list in lists) {
            val enabled = !loading && !toggling.contains(list.id)
            ListButton(
                text = list.name,
                enabled = enabled,
                checked = showLists.contains(list.id),
                modifier = Modifier
                    .fillMaxWidth()
                    .onClick(enabled = enabled) {
                        onListClick?.invoke(list)
                    },
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@DevicePreview
@Composable
private fun Preview() {
    TraktThemeLightDark {
        ShowDetailsListsContent(
            show = PreviewData.show1,
            loading = LoadingState.Done,
            inWatchlist = true,
            lists = listOf(PreviewData.customListMinimal1).toImmutableList(),
            showLists = setOf(PreviewData.show1.ids.trakt).toImmutableSet(),
            toggling = emptySet<TraktId>().toImmutableSet(),
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@DevicePreview
@Composable
private fun Preview2() {
    TraktThemeLightDark {
        ShowDetailsListsContent(
            show = PreviewData.show1,
            loading = LoadingState.Done,
            inWatchlist = false,
            lists = listOf(PreviewData.customListMinimal1).toImmutableList(),
            showLists = emptySet<TraktId>().toImmutableSet(),
            toggling = emptySet<TraktId>().toImmutableSet(),
        )
    }
}
