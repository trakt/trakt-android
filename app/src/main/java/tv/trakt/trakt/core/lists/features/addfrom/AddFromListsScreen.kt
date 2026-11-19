package tv.trakt.trakt.core.lists.features.addfrom

import android.content.res.Resources
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.core.lists.usecases.toUserMessage
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsItem
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsPick
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsSource
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

private const val LISTED_ALPHA = 0.4F
private val FOOTER_CLEARANCE = 104.dp
private val FooterShape = RoundedCornerShape(100)
private val SelectorHeight = 56.dp
private val SelectorShape = RoundedCornerShape(16.dp)

@Composable
internal fun AddFromListsScreen(
    viewModel: AddFromListsViewModel,
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val bottomBarVisibility = LocalBottomBarVisibility.current

    DisposableEffect(Unit) {
        bottomBarVisibility.value = false
        onDispose {
            bottomBarVisibility.value = true
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.results.collect { result ->
            onShowMessage(result.toMessage(resources, state.listName))
            onNavigateBack()
        }
    }

    AddFromListsContent(
        state = state,
        onBackClick = onNavigateBack,
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
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSourceClick: (AddFromListsSource) -> Unit = {},
    onItemClick: (AddFromListsItem) -> Unit = {},
    onToggleAllClick: () -> Unit = {},
    onLoadMoreClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val density = LocalDensity.current
    var selectorHalfHeight by remember { mutableStateOf(SelectorHeight / 2) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        ) {
            TitleBar(
                title = state.listName,
                onBackClick = onBackClick,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Box(modifier = Modifier.weight(1F)) {
                ItemsList(
                    state = state,
                    onItemClick = onItemClick,
                    onToggleAllClick = onToggleAllClick,
                    onLoadMoreClick = onLoadMoreClick,
                    contentPadding = PaddingValues(
                        top = selectorHalfHeight + 12.dp,
                        bottom = bottomInset + FOOTER_CLEARANCE,
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = selectorHalfHeight),
                )

                val selectorModifier = Modifier.onSizeChanged {
                    selectorHalfHeight = with(density) { (it.height / 2).toDp() }
                }

                when (val activeSource = state.activeSource) {
                    null -> SourceSelectPlaceholder(modifier = selectorModifier)
                    else -> SourceSelect(
                        sources = state.sources,
                        activeSource = activeSource,
                        pickedCounts = state.pickedCounts,
                        alreadyInList = state.alreadyInListCount
                            .takeIf { it > 0 }
                            ?.let {
                                stringResource(
                                    R.string.text_add_from_lists_already_in_list,
                                    it.toString(),
                                    state.listName,
                                )
                            },
                        onSourceClick = onSourceClick,
                        modifier = selectorModifier,
                    )
                }
            }
        }

        val bottomColor = TraktTheme.colors.backgroundPrimary
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomInset + FOOTER_CLEARANCE)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            bottomColor.copy(alpha = 0.98F),
                            bottomColor,
                        ),
                    ),
                ),
        )

        Footer(
            state = state,
            onAddClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomInset + 16.dp),
        )
    }
}

@Composable
private fun TitleBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -2.dp.toPx()
            },
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .weight(1F, fill = false)
                .onClick(onClick = onBackClick),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )

            TraktHeader(
                title = title,
                subtitle = stringResource(R.string.text_add_from_lists_description),
                modifier = Modifier.weight(1F, fill = false),
            )
        }
    }
}

@Composable
private fun SourceSelect(
    sources: ImmutableList<AddFromListsSource>,
    activeSource: AddFromListsSource,
    pickedCounts: ImmutableMap<String, Int>,
    alreadyInList: String?,
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
                .heightIn(min = SelectorHeight)
                .shadow(TraktTheme.colors.shadowSmall, SelectorShape)
                .background(TraktTheme.colors.panelCardContainer, SelectorShape)
                .onClick { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Column(
                verticalArrangement = spacedBy(1.dp),
                modifier = Modifier.weight(1F),
            ) {
                Text(
                    text = activeSource.name(),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.buttonPrimary,
                    maxLines = 1,
                    overflow = Ellipsis,
                )

                alreadyInList?.let {
                    Text(
                        text = it,
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.paragraphSmaller.copy(
                            fontSize = 12.sp,
                        ),
                        maxLines = 1,
                        overflow = Ellipsis,
                    )
                }
            }

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

        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
            DropdownMenu(
                expanded = expanded,
                containerColor = TraktTheme.colors.dialogContainer,
                shape = RoundedCornerShape(16.dp),
                offset = DpOffset(x = 0.dp, y = 8.dp),
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
}

@Composable
private fun SourceSelectPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SelectorHeight)
            .shadow(TraktTheme.colors.shadowSmall, SelectorShape)
            .background(TraktTheme.colors.panelCardContainer, SelectorShape),
    )
}

@Composable
private fun SourceCounts(
    source: AddFromListsSource,
    picked: Int,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(11.dp),
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
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val items = state.items.takeIf { state.listedKeys != null }.orEmpty()

    LazyColumn(
        verticalArrangement = spacedBy(2.dp),
        contentPadding = contentPadding,
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

        if (items.isNotEmpty() && (state.candidates.isNotEmpty() || state.hasMoreItems)) {
            item {
                Row(
                    horizontalArrangement = spacedBy(12.dp, Alignment.CenterHorizontally),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                ) {
                    if (state.candidates.isNotEmpty()) {
                        OutlineButton(
                            text = stringResource(
                                when {
                                    state.isAllPicked -> R.string.button_text_deselect_all
                                    else -> R.string.button_text_select_all
                                },
                            ),
                            onClick = onToggleAllClick,
                        )
                    }

                    if (state.hasMoreItems) {
                        OutlineButton(
                            text = stringResource(R.string.button_text_load_more),
                            loading = state.loadingMore.isLoading,
                            onClick = onLoadMoreClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    loading: Boolean = false,
) {
    PrimaryButton(
        text = text,
        textStyle = TraktTheme.typography.buttonTertiary,
        height = 32.dp,
        contentPadding = 18.dp,
        iconSize = 14.dp,
        loading = loading,
        enabled = !loading,
        border = BorderStroke(1.dp, TraktTheme.colors.chipContainer),
        containerColor = Color.Transparent,
        contentColor = TraktTheme.colors.textPrimary,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = TraktTheme.colors.textPrimary,
        onClick = onClick,
    )
}

@Composable
private fun ItemsPlaceholder(state: AddFromListsState) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
    ) {
        when {
            state.error == null && (state.loading.isLoading || state.items == null || state.listedKeys == null) -> {
                FilmProgressIndicator(
                    color = TraktTheme.colors.accent,
                    modifier = Modifier.size(24.dp),
                )
                CircularProgressIndicator(
                    color = TraktTheme.colors.accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp),
                )
            }
            else -> {
                Text(
                    text = when (state.error) {
                        null -> stringResource(R.string.text_no_titles_to_add)
                        else -> stringResource(R.string.error_text_unexpected_error_short)
                    },
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
            .onClick(enabled = !listed, throttle = false, onClick = onClick)
            .padding(vertical = 4.dp),
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
                width = 48.dp,
                corner = 10.dp,
                more = false,
            )

            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Text(
                    text = item.title,
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.cardTitle.copy(fontSize = 14.sp),
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
                style = TraktTheme.typography.meta,
                modifier = Modifier.padding(end = 2.dp),
            )
        } else {
            PickCheck(picked = picked)
        }
    }
}

@Composable
private fun PickCheck(picked: Boolean) {
    Checkbox(
        checked = picked,
        onCheckedChange = null,
        enabled = false,
        colors = CheckboxDefaults.colors().copy(
            checkedCheckmarkColor = TraktTheme.colors.textPrimaryOnAccent,
            disabledUncheckedBoxColor = Color.Transparent,
            disabledUncheckedBorderColor = TraktTheme.colors.textPrimary,
            disabledCheckedBoxColor = TraktTheme.colors.accent,
            disabledBorderColor = TraktTheme.colors.accent,
        ),
    )
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
        modifier = modifier
            .fillMaxWidth()
            .shadow(TraktTheme.colors.shadowSmall, FooterShape)
            .background(TraktTheme.colors.panelCardContainer, FooterShape)
            .padding(start = 22.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Column(
            verticalArrangement = spacedBy(1.dp),
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
                style = TraktTheme.typography.paragraphSmaller.copy(
                    fontSize = 12.sp,
                ),
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
            modifier = Modifier.widthIn(min = 92.dp),
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
            state = AddFromListsState(
                listName = "Weekend Picks",
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
            state = AddFromListsState(
                listName = "Weekend Picks",
                sources = persistentListOf(AddFromListsSource.Watchlist),
                activeSource = AddFromListsSource.Watchlist,
                items = persistentListOf(),
                listedKeys = persistentSetOf(),
                loading = LoadingState.Done,
            ),
        )
    }
}

private fun AddFromListsResult.toMessage(
    resources: Resources,
    listName: String,
): String {
    return when (this) {
        is AddFromListsResult.Added -> when (count) {
            1 -> resources.getString(R.string.text_added_title_to_list, listName)
            else -> resources.getString(R.string.text_added_titles_to_list, count.toString(), listName)
        }
        is AddFromListsResult.LimitReached -> error.toUserMessage(resources)
        is AddFromListsResult.Failed -> resources.getString(R.string.text_failed_to_add_titles, listName)
    }
}
