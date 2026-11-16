package tv.trakt.trakt.app.core.details.lists.manage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush.Companion.horizontalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.app.LocalSnackbarState
import tv.trakt.trakt.app.common.ui.buttons.OutlineButton
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.core.lists.manage.ManageListsState
import tv.trakt.trakt.common.core.lists.manage.ManageListsTarget
import tv.trakt.trakt.common.core.lists.manage.ManageListsTarget.EpisodeTarget
import tv.trakt.trakt.common.core.lists.manage.ManageListsTarget.MovieTarget
import tv.trakt.trakt.common.core.lists.manage.ManageListsTarget.SeasonTarget
import tv.trakt.trakt.common.core.lists.manage.ManageListsTarget.ShowTarget
import tv.trakt.trakt.common.core.lists.manage.ManageListsViewModel
import tv.trakt.trakt.common.core.lists.usecases.toUserMessage
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.CustomListMinimal
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R
import kotlin.random.Random.Default.nextInt

private val panelShape = RoundedCornerShape(24.dp)
private val rowShape = RoundedCornerShape(12.dp)
private val rowHeight = 42.dp
private val checkboxShape = RoundedCornerShape(3.dp)
private val checkboxSize = 16.dp

// Rows sit closer to the panel edge than text, so the checkbox lines up with the headers.
private val rowInset = 12.dp
private val textInset = 12.dp

@Composable
internal fun ManageListsOverlay(
    target: ManageListsTarget?,
    onListOpen: (list: CustomList, isOwner: Boolean) -> Unit,
    onExited: () -> Unit,
) {
    AnimatedVisibility(
        visible = target != null,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        if (target != null) {
            val viewModelKey = remember(target) { nextInt().toString() }
            val lifecycle = LocalLifecycleOwner.current.lifecycle

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9F))),
                    ),
            ) {
                ManageListsPanel(
                    target = target,
                    viewModel = koinViewModel(
                        key = viewModelKey,
                        parameters = { parametersOf(target) },
                    ),
                    onListOpen = onListOpen,
                    modifier = Modifier
                        .padding(32.dp)
                        .width(400.dp)
                        .align(Alignment.TopEnd)
                        .animateEnterExit(
                            enter = fadeIn() + slideInHorizontally { it / 10 },
                            exit = ExitTransition.None,
                        )
                        .focusProperties {
                            onExit = {
                                // Focus moving to the next screen after opening a list must not close the panel here.
                                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                    onExited()
                                }
                            }
                        }
                        .focusGroup(),
                )
            }
        }
    }
}

@Composable
private fun ManageListsPanel(
    target: ManageListsTarget,
    viewModel: ManageListsViewModel,
    onListOpen: (list: CustomList, isOwner: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val localSnack = LocalSnackbarState.current
    val localRes = LocalResources.current

    LaunchedEffect(viewModel) {
        viewModel.openListEvents.collect { list ->
            onListOpen(list, list.user.ids.trakt == viewModel.state.value.user?.ids?.trakt)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.itemLimitEvents.collect { limit ->
            localSnack.showSnackbar(limit.toUserMessage(localRes))
        }
    }

    ManageListsContent(
        target = target,
        state = state,
        onListClick = viewModel::toggleList,
        onListOpenClick = viewModel::openList,
        modifier = modifier,
    )
}

@Composable
private fun ManageListsContent(
    target: ManageListsTarget,
    state: ManageListsState,
    modifier: Modifier = Modifier,
    onListClick: (CustomListMinimal) -> Unit = {},
    onListOpenClick: (CustomListMinimal) -> Unit = {},
) {
    val headerFocusRequester = remember { FocusRequester() }
    val firstListFocusRequester = remember { FocusRequester() }
    val firstListId = (state.lists.firstOrNull() ?: state.collaborations.firstOrNull())?.id

    LaunchedEffect(firstListId) {
        when (firstListId) {
            null -> headerFocusRequester.requestFocus()
            else -> firstListFocusRequester.requestFocus()
        }
    }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 4.dp,
                shape = panelShape,
            )
            .clip(panelShape)
            .verticalScroll(
                state = rememberScrollState(),
                overscrollEffect = null,
            )
            .background(TraktTheme.colors.commentContainer)
            .padding(vertical = 20.dp, horizontal = rowInset),
    ) {
        Column(
            modifier = Modifier
                .focusRequester(headerFocusRequester)
                .focusable(enabled = firstListId == null)
                .padding(horizontal = textInset),
        ) {
            Text(
                text = stringResource(R.string.header_manage_lists),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading4,
            )

            Text(
                text = target.title(),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (state.loading.isLoading && firstListId == null) {
            FilmProgressIndicator(
                size = 24.dp,
                color = TraktTheme.colors.textSecondary,
                modifier = Modifier.padding(start = textInset, top = 24.dp),
            )
        }

        ListsSection(
            title = stringResource(R.string.list_title_personal_lists),
            lists = state.lists,
            state = state,
            firstListId = firstListId,
            firstListFocusRequester = firstListFocusRequester,
            onListClick = onListClick,
            onListOpenClick = onListOpenClick,
        )

        ListsSection(
            title = stringResource(R.string.list_title_collaborative_lists),
            lists = state.collaborations,
            state = state,
            firstListId = firstListId,
            firstListFocusRequester = firstListFocusRequester,
            onListClick = onListClick,
            onListOpenClick = onListOpenClick,
        )
    }
}

@Composable
private fun ListsSection(
    title: String,
    lists: ImmutableList<CustomListMinimal>,
    state: ManageListsState,
    firstListId: TraktId?,
    firstListFocusRequester: FocusRequester,
    onListClick: (CustomListMinimal) -> Unit,
    onListOpenClick: (CustomListMinimal) -> Unit,
) {
    if (lists.isEmpty()) return

    Text(
        text = title,
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(start = textInset, top = 24.dp, bottom = 12.dp),
    )

    Column(
        verticalArrangement = spacedBy(8.dp),
    ) {
        for (list in lists) {
            ListRow(
                list = list,
                checked = state.listedIds.contains(list.id),
                toggling = state.loading.isLoading || state.toggling.contains(list.id),
                opening = state.opening == list.id,
                onClick = onListClick,
                onOpenClick = onListOpenClick,
                toggleModifier = when (list.id) {
                    firstListId -> Modifier.focusRequester(firstListFocusRequester)
                    else -> Modifier
                },
            )
        }
    }
}

@Composable
private fun ListRow(
    list: CustomListMinimal,
    checked: Boolean,
    toggling: Boolean,
    opening: Boolean,
    onClick: (CustomListMinimal) -> Unit,
    onOpenClick: (CustomListMinimal) -> Unit,
    toggleModifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        ListToggle(
            text = list.name,
            checked = checked,
            toggling = toggling,
            onClick = { onClick(list) },
            modifier = toggleModifier.weight(1F),
        )

        OutlineButton(
            icon = painterResource(R.drawable.ic_chevron_right),
            iconSize = 18.dp,
            loading = opening,
            onClick = { onOpenClick(list) },
        )
    }
}

@Composable
private fun ListToggle(
    text: String,
    checked: Boolean,
    toggling: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = rowShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = TraktTheme.colors.textPrimary,
            focusedContainerColor = Color.Transparent,
            focusedContentColor = TraktTheme.colors.textPrimary,
            pressedContainerColor = Color.Transparent,
            pressedContentColor = TraktTheme.colors.textPrimary,
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                border = BorderStroke(width = 1.5.dp, color = TraktTheme.colors.primaryButtonContainerDisabled),
                shape = rowShape,
            ),
            focusedBorder = Border(
                border = BorderStroke(width = (2.75).dp, color = TraktTheme.colors.textPrimary),
                shape = rowShape,
            ),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1F),
        modifier = modifier.height(rowHeight),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = textInset),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(checkboxSize),
            ) {
                if (toggling) {
                    FilmProgressIndicator(
                        size = 14.dp,
                        color = TraktTheme.colors.textSecondary,
                    )
                } else {
                    ListCheckbox(checked = checked)
                }
            }

            Text(
                text = text.uppercase(),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ListCheckbox(checked: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(checkboxSize)
            .background(
                color = if (checked) TraktTheme.colors.textPrimary else Color.Transparent,
                shape = checkboxShape,
            )
            .border(
                width = 1.5.dp,
                color = TraktTheme.colors.textPrimary,
                shape = checkboxShape,
            ),
    ) {
        if (checked) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = TraktTheme.colors.commentContainer,
                modifier = Modifier.size(12.dp),
            )
        }
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
        is EpisodeTarget -> "${show.title} - ${episode.title}"
    }
}

@Preview(widthDp = 440, heightDp = 540)
@Composable
private fun Preview() {
    TraktTheme {
        ManageListsContent(
            target = MovieTarget(movie = PreviewData.movie1),
            state = ManageListsState(
                loading = LoadingState.Done,
                lists = persistentListOf(
                    PreviewData.customListMinimal1,
                    PreviewData.customListMinimal1.copy(
                        id = TraktId(2),
                        name = "Another List With A Very Long Name",
                    ),
                ),
                collaborations = persistentListOf(
                    PreviewData.customListMinimal1.copy(
                        id = TraktId(3),
                        ownerId = TraktId(3),
                        name = "Shared List",
                    ),
                ),
                listedIds = persistentSetOf(PreviewData.customListMinimal1.id),
                toggling = persistentSetOf(TraktId(3)),
                opening = TraktId(2),
            ),
        )
    }
}

@Preview(widthDp = 440, heightDp = 300)
@Composable
private fun LoadingPreview() {
    TraktTheme {
        ManageListsContent(
            target = MovieTarget(movie = PreviewData.movie1),
            state = ManageListsState(
                loading = LoadingState.Loading,
            ),
        )
    }
}
