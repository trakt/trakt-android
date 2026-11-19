package tv.trakt.trakt.core.lists.features.addfrom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsItem
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsPick
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsSource
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

private const val LISTED_ALPHA = 0.45F
private const val PICKED_ALPHA = 0.13F

@Composable
internal fun AddFromListsView(
    listName: String,
    viewModel: AddFromListsViewModel,
    onResult: (AddFromListsResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.results.collect(onResult)
    }

    AddFromListsContent(
        state = state,
        listName = listName,
        onSourceClick = viewModel::selectSource,
        onItemClick = viewModel::togglePick,
        onToggleAllClick = { viewModel.toggleAllPicks(state.candidates) },
        onLoadMoreClick = viewModel::loadMoreItems,
        onAddClick = viewModel::addPicks,
        modifier = modifier,
    )
}

@Composable
private fun AddFromListsContent(
    state: AddFromListsState,
    listName: String,
    modifier: Modifier = Modifier,
    onSourceClick: (AddFromListsSource) -> Unit = {},
    onItemClick: (AddFromListsItem) -> Unit = {},
    onToggleAllClick: () -> Unit = {},
    onLoadMoreClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxHeight(),
    ) {
        Text(
            text = stringResource(R.string.header_add_from_lists, listName),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading2,
            maxLines = 1,
            overflow = Ellipsis,
        )

        Text(
            text = stringResource(R.string.text_add_from_lists_description),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmaller,
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )

        state.activeSource?.let { activeSource ->
            SourceSelect(
                sources = state.sources,
                activeSource = activeSource,
                pickedCounts = state.pickedCounts,
                onSourceClick = onSourceClick,
                modifier = Modifier.padding(top = 18.dp),
            )
        }

        if (state.alreadyInListCount > 0) {
            Text(
                text = stringResource(
                    R.string.text_add_from_lists_already_in_list,
                    state.alreadyInListCount.toString(),
                    listName,
                ),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
                maxLines = 1,
                overflow = Ellipsis,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        ItemsList(
            state = state,
            onItemClick = onItemClick,
            onToggleAllClick = onToggleAllClick,
            onLoadMoreClick = onLoadMoreClick,
            modifier = Modifier
                .weight(1F)
                .padding(top = 8.dp),
        )

        Spacer(
            modifier = Modifier
                .background(TraktTheme.colors.separator)
                .fillMaxWidth()
                .height(1.dp),
        )

        Footer(
            state = state,
            onAddClick = onAddClick,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun SourceSelect(
    sources: ImmutableList<AddFromListsSource>,
    activeSource: AddFromListsSource,
    pickedCounts: ImmutableMap<String, Int>,
    onSourceClick: (AddFromListsSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TraktTheme.colors.dialogOnContainer)
                .onClick { expanded = !expanded }
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = activeSource.name(),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonPrimary,
                maxLines = 1,
                overflow = Ellipsis,
                modifier = Modifier.weight(1F),
            )

            SourceCounts(
                source = activeSource,
                picked = pickedCounts[activeSource.key] ?: 0,
            )

            Icon(
                painter = painterResource(R.drawable.ic_arrow_dropdown),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(if (expanded) 180F else 0F),
            )
        }

        DropdownMenu(
            expanded = expanded,
            containerColor = TraktTheme.colors.dialogOnContainer,
            shape = RoundedCornerShape(16.dp),
            onDismissRequest = { expanded = false },
        ) {
            sources.forEach { source ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = source.name(),
                            style = TraktTheme.typography.buttonTertiary,
                            color = when (source.key) {
                                activeSource.key -> TraktTheme.colors.accent
                                else -> TraktTheme.colors.textPrimary
                            },
                            maxLines = 1,
                            overflow = Ellipsis,
                        )
                    },
                    trailingIcon = {
                        SourceCounts(
                            source = source,
                            picked = pickedCounts[source.key] ?: 0,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSourceClick(source)
                    },
                )
            }
        }
    }
}

@Composable
private fun SourceCounts(
    source: AddFromListsSource,
    picked: Int,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(6.dp),
    ) {
        if (picked > 0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                    .clip(CircleShape)
                    .background(TraktTheme.colors.accent)
                    .padding(horizontal = 4.dp),
            ) {
                Text(
                    text = picked.toString(),
                    color = TraktTheme.colors.textPrimaryOnAccent,
                    style = TraktTheme.typography.meta,
                )
            }
        }

        if (source is AddFromListsSource.PersonalList) {
            Text(
                text = source.count.toString(),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
            )
        }
    }
}

@Composable
private fun ItemsList(
    state: AddFromListsState,
    onItemClick: (AddFromListsItem) -> Unit,
    onToggleAllClick: () -> Unit,
    onLoadMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = state.items.orEmpty()

    LazyColumn(
        verticalArrangement = spacedBy(2.dp),
        overscrollEffect = null,
        modifier = modifier,
    ) {
        items(
            items = items,
            key = { it.key },
        ) { item ->
            ItemRow(
                item = item,
                picked = item.key in state.pickedKeys,
                listed = state.isListed(item),
                onClick = { onItemClick(item) },
            )
        }

        if (items.isEmpty()) {
            item {
                ItemsPlaceholder(state = state)
            }
        }

        if (state.candidates.isNotEmpty() || state.hasMoreItems) {
            item {
                Row(
                    horizontalArrangement = spacedBy(12.dp, Alignment.CenterHorizontally),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                ) {
                    if (state.candidates.isNotEmpty()) {
                        TertiaryButton(
                            text = stringResource(
                                when {
                                    state.isAllPicked -> R.string.button_text_deselect_all
                                    else -> R.string.button_text_select_all
                                },
                            ),
                            containerColor = TraktTheme.colors.dialogOnContainer,
                            contentColor = TraktTheme.colors.textPrimary,
                            onClick = onToggleAllClick,
                        )
                    }

                    if (state.hasMoreItems) {
                        TertiaryButton(
                            text = stringResource(R.string.button_text_load_more),
                            loading = state.loadingMore.isLoading,
                            enabled = !state.loadingMore.isLoading,
                            containerColor = TraktTheme.colors.dialogOnContainer,
                            contentColor = TraktTheme.colors.textPrimary,
                            onClick = onLoadMoreClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemsPlaceholder(state: AddFromListsState) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
    ) {
        when {
            state.error == null && (state.loading.isLoading || state.items == null) -> {
                CircularProgressIndicator(
                    color = TraktTheme.colors.accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp),
                )
            }
            else -> {
                Text(
                    text = state.error?.localizedMessage
                        ?: stringResource(R.string.text_no_titles_to_add),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmall,
                )
            }
        }
    }
}

@Composable
private fun ItemRow(
    item: AddFromListsItem,
    picked: Boolean,
    listed: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    picked -> TraktTheme.colors.accent.copy(alpha = PICKED_ALPHA)
                    else -> TraktTheme.colors.dialogContainer
                },
            )
            .onClick(enabled = !listed, throttle = false, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .weight(1F)
                .alpha(if (listed) LISTED_ALPHA else 1F),
        ) {
            VerticalMediaCard(
                title = "",
                imageUrl = item.posterUrl,
                width = 40.dp,
                corner = 8.dp,
                more = false,
            )

            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Text(
                    text = item.title,
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.cardTitle,
                    maxLines = 1,
                    overflow = Ellipsis,
                )

                item.year?.let {
                    Text(
                        text = it.toString(),
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.cardSubtitle,
                        maxLines = 1,
                    )
                }
            }
        }

        if (listed) {
            Text(
                text = stringResource(R.string.text_already_in_list),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
            )
        } else {
            PickCheck(picked = picked)
        }
    }
}

@Composable
private fun PickCheck(picked: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (picked) TraktTheme.colors.accent else TraktTheme.colors.dialogContainer)
            .border(
                width = 2.dp,
                color = if (picked) TraktTheme.colors.accent else TraktTheme.colors.textSecondary,
                shape = CircleShape,
            ),
    ) {
        if (picked) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimaryOnAccent,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun Footer(
    state: AddFromListsState,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val count = state.effectivePicks.size
    val listCount = state.pickedCounts.size

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = spacedBy(2.dp),
            modifier = Modifier.weight(1F),
        ) {
            Text(
                text = when (count) {
                    0 -> stringResource(R.string.text_nothing_selected)
                    else -> stringResource(R.string.text_titles_selected, count.toString())
                },
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonPrimary,
                maxLines = 1,
                overflow = Ellipsis,
            )

            Text(
                text = when {
                    listCount > 1 -> stringResource(R.string.text_selected_from_lists, listCount.toString())
                    else -> stringResource(R.string.text_pick_from_other_lists_hint)
                },
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
                maxLines = 1,
                overflow = Ellipsis,
            )
        }

        PrimaryButton(
            text = when (count) {
                0 -> stringResource(R.string.button_text_add_selected_none)
                else -> stringResource(R.string.button_text_add_selected, count.toString())
            },
            enabled = state.isAddEnabled,
            loading = state.adding.isLoading,
            onClick = onAddClick,
        )
    }
}

@Composable
private fun AddFromListsSource.name(): String {
    return when (this) {
        is AddFromListsSource.Watchlist -> stringResource(R.string.list_title_watchlist)
        is AddFromListsSource.PersonalList -> name
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    val source = AddFromListsSource.PersonalList(
        id = TraktId(2),
        name = "Favorite Sci-Fi",
        count = 24,
    )
    val items = persistentListOf(
        AddFromListsItem(
            type = MediaType.Movie,
            id = TraktId(1),
            title = "Arrival",
            year = 2016,
            posterUrl = null,
        ),
        AddFromListsItem(
            type = MediaType.Show,
            id = TraktId(2),
            title = "Severance",
            year = 2022,
            posterUrl = null,
        ),
        AddFromListsItem(
            type = MediaType.Movie,
            id = TraktId(3),
            title = "Interstellar",
            year = 2014,
            posterUrl = null,
        ),
    )

    TraktThemePreview {
        AddFromListsContent(
            listName = "Weekend Picks",
            state = AddFromListsState(
                sources = persistentListOf(AddFromListsSource.Watchlist, source),
                activeSource = source,
                items = items,
                hasMoreItems = true,
                listedKeys = persistentSetOf(items[2].key),
                picks = persistentListOf(AddFromListsPick.from(items[0], source.key)),
                loading = LoadingState.Done,
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun EmptyPreview() {
    TraktThemePreview {
        AddFromListsContent(
            listName = "Weekend Picks",
            state = AddFromListsState(
                sources = persistentListOf(AddFromListsSource.Watchlist),
                activeSource = AddFromListsSource.Watchlist,
                items = persistentListOf(),
                listedKeys = persistentSetOf(),
                loading = LoadingState.Done,
            ),
        )
    }
}
