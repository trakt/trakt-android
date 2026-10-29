@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.lists.features.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomListMinimal
import tv.trakt.trakt.core.lists.features.manage.ManageListsTarget.EpisodeTarget
import tv.trakt.trakt.core.lists.features.manage.ManageListsTarget.MovieTarget
import tv.trakt.trakt.core.lists.features.manage.ManageListsTarget.SeasonTarget
import tv.trakt.trakt.core.lists.features.manage.ManageListsTarget.ShowTarget
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.lists.ListButton
import tv.trakt.trakt.ui.components.buttons.lists.WatchlistButton
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ManageListsView(
    target: ManageListsTarget,
    viewModel: ManageListsViewModel,
    inWatchlist: Boolean,
    modifier: Modifier = Modifier,
    onWatchlistClick: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }

    ManageListsContent(
        target = target,
        state = state,
        inWatchlist = inWatchlist,
        onWatchlistClick = onWatchlistClick?.let { onClick ->
            {
                if (inWatchlist) {
                    confirmRemoveWatchlistSheet = true
                } else {
                    onClick()
                }
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
            target.title(),
        ),
    )
}

@Composable
private fun ManageListsContent(
    target: ManageListsTarget,
    state: ManageListsState,
    modifier: Modifier = Modifier,
    inWatchlist: Boolean = false,
    onWatchlistClick: (() -> Unit)? = null,
    onListClick: ((CustomListMinimal) -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Text(
            text = target.title(),
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
            text = target.subtitle(),
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

        ListButtons(
            loading = state.loading.isLoading,
            inWatchlist = inWatchlist,
            onWatchlistClick = onWatchlistClick,
            lists = state.lists,
            collaborations = state.collaborations,
            listedIds = state.listedIds,
            toggling = state.toggling,
            onListClick = onListClick,
            modifier = Modifier
                .padding(top = 12.dp),
        )
    }
}

@Composable
private fun ManageListsTarget.title(): String {
    return when (this) {
        is MovieTarget -> movie.title
        is ShowTarget -> show.title
        is SeasonTarget -> when (season.number) {
            0 -> stringResource(R.string.text_season_specials)
            else -> stringResource(R.string.text_season_number, season.number)
        }
        is EpisodeTarget -> episode.title
    }
}

@Composable
private fun ManageListsTarget.subtitle(): String {
    return when (this) {
        is MovieTarget -> "${movie.released?.year ?: movie.year}  •  ${genresText(movie.genres)}"
        is ShowTarget -> "${show.releasedAt?.toLocal()?.year ?: show.year}  •  ${genresText(show.genres)}"
        is SeasonTarget -> show.title
        is EpisodeTarget -> "${show.title}  •  ${episode.seasonEpisode.toDisplayString()}"
    }
}

@Composable
private fun genresText(genres: List<MediaGenre>): String {
    return genres.take(2)
        .map { stringResource(it.displayStringRes) }
        .joinToString(", ")
}

@Composable
private fun ListButtons(
    loading: Boolean,
    inWatchlist: Boolean,
    lists: ImmutableList<CustomListMinimal>,
    collaborations: ImmutableList<CustomListMinimal>,
    listedIds: ImmutableSet<TraktId>,
    toggling: ImmutableSet<TraktId>,
    modifier: Modifier = Modifier,
    onWatchlistClick: (() -> Unit)? = null,
    onListClick: ((CustomListMinimal) -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier
            .verticalScroll(
                state = rememberScrollState(),
                overscrollEffect = null,
            ),
    ) {
        if (onWatchlistClick != null) {
            WatchlistButton(
                checked = inWatchlist,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationX = -4.dp.toPx()
                    }
                    .onClick(onClick = onWatchlistClick),
            )
        }

        if (lists.isNotEmpty() || loading) {
            ListsHeader(
                text = stringResource(R.string.list_title_personal_lists),
            )
        }

        for (list in lists) {
            ListToggle(
                list = list,
                enabled = !loading && !toggling.contains(list.id),
                checked = listedIds.contains(list.id),
                onClick = onListClick,
            )
        }

        if (collaborations.isNotEmpty()) {
            ListsHeader(
                text = stringResource(R.string.list_title_collaborative_lists),
            )
        }

        for (list in collaborations) {
            ListToggle(
                list = list,
                enabled = !loading && !toggling.contains(list.id),
                checked = listedIds.contains(list.id),
                onClick = onListClick,
            )
        }
    }
}

@Composable
private fun ListToggle(
    list: CustomListMinimal,
    enabled: Boolean,
    checked: Boolean,
    onClick: ((CustomListMinimal) -> Unit)?,
) {
    ListButton(
        text = list.name,
        enabled = enabled,
        checked = checked,
        modifier = Modifier
            .fillMaxWidth()
            .onClick(enabled = enabled) {
                onClick?.invoke(list)
            },
    )
}

@Composable
private fun ListsHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading5.copy(
            fontSize = 16.sp,
        ),
        maxLines = 1,
        overflow = Ellipsis,
        textAlign = TextAlign.Start,
        modifier = modifier
            .padding(vertical = 8.dp),
    )
}

@PreviewTraktLightDark
@Composable
private fun MoviePreview() {
    TraktThemePreview {
        ManageListsContent(
            target = MovieTarget(
                movie = PreviewData.movie1,
            ),
            state = ManageListsState(
                loading = LoadingState.Done,
                lists = persistentListOf(PreviewData.customListMinimal1),
                listedIds = persistentSetOf(PreviewData.customListMinimal1.id),
            ),
            inWatchlist = true,
            onWatchlistClick = {},
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun ShowPreview() {
    TraktThemePreview {
        ManageListsContent(
            target = ShowTarget(
                show = PreviewData.show1,
            ),
            state = ManageListsState(
                loading = LoadingState.Done,
                lists = persistentListOf(PreviewData.customListMinimal1),
            ),
            onWatchlistClick = {},
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun EpisodePreview() {
    TraktThemePreview {
        ManageListsContent(
            target = EpisodeTarget(
                show = PreviewData.show1,
                episode = PreviewData.episode1,
            ),
            state = ManageListsState(
                loading = LoadingState.Done,
                lists = persistentListOf(PreviewData.customListMinimal1),
                collaborations = persistentListOf(
                    PreviewData.customListMinimal1.copy(
                        id = TraktId(2),
                        ownerId = TraktId(2),
                        name = "Shared List",
                    ),
                ),
                listedIds = persistentSetOf(PreviewData.customListMinimal1.id),
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun SeasonPreview() {
    TraktThemePreview {
        ManageListsContent(
            target = SeasonTarget(
                show = PreviewData.show1,
                season = PreviewData.season1,
            ),
            state = ManageListsState(
                loading = LoadingState.Done,
                lists = persistentListOf(PreviewData.customListMinimal1),
                collaborations = EmptyImmutableList,
            ),
        )
    }
}
