package tv.trakt.trakt.core.lists.features.convert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsMovieView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsShowView
import tv.trakt.trakt.core.lists.model.SmartListItem
import tv.trakt.trakt.core.lists.model.SmartListItem.MovieItem
import tv.trakt.trakt.core.lists.model.SmartListItem.ShowItem
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

private val FooterClearance = 132.dp

@Composable
internal fun SmartListConvertScreen(
    viewModel: SmartListConvertViewModel,
    onNavigateBack: () -> Unit,
    onConverted: (SmartList) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snack = LocalSnackbarState.current
    val bottomBarVisibility = LocalBottomBarVisibility.current
    val errorMessage = stringResource(R.string.action_toast_action_failed)

    DisposableEffect(Unit) {
        bottomBarVisibility.value = false
        onDispose {
            bottomBarVisibility.value = true
        }
    }

    LaunchedEffect(state.missing) {
        if (state.missing) {
            onNavigateBack()
        }
    }

    LaunchedEffect(state.convertedList) {
        state.convertedList?.let(onConverted)
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snack.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long,
            )
            viewModel.clearError()
        }
    }

    SmartListConvertContent(
        state = state,
        onConvertClick = viewModel::convert,
        onCancelClick = onNavigateBack,
    )
}

@Composable
private fun SmartListConvertContent(
    state: SmartListConvertState,
    modifier: Modifier = Modifier,
    onConvertClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
) {
    var showProposed by rememberSaveable { mutableStateOf(true) }
    val listState = rememberLazyListState()

    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding(),
        bottom = bottomInset + FooterClearance,
    )

    val items = when {
        showProposed -> state.proposedItems
        else -> state.currentItems
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        ScrollableBackdropImage(scrollState = listState)

        LazyColumn(
            state = listState,
            contentPadding = contentPadding,
            overscrollEffect = null,
        ) {
            item(key = "title") {
                TitleBar(
                    title = state.list?.name.orEmpty(),
                    subtitle = state.draft?.rememberSummary(),
                    onClick = onCancelClick,
                )
            }

            item(key = "toggle") {
                Row(
                    horizontalArrangement = spacedBy(TraktTheme.spacing.chipsSpace),
                    modifier = Modifier.padding(
                        top = 2.dp,
                        bottom = TraktTheme.spacing.mainListVerticalSpace,
                    ),
                ) {
                    FilterChip(
                        selected = !showProposed,
                        text = stringResource(R.string.header_smart_list_compare_current),
                        height = 30.dp,
                        onClick = { showProposed = false },
                    )
                    FilterChip(
                        selected = showProposed,
                        text = stringResource(R.string.header_smart_list_compare_proposed),
                        height = 30.dp,
                        onClick = { showProposed = true },
                    )
                }
            }

            compareItems(
                items = items,
                loading = state.loading.isLoading || items == null,
            )
        }

        val bottomColor = TraktTheme.colors.backgroundPrimary
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomInset + FooterClearance)
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

        PrimaryButton(
            text = stringResource(R.string.button_text_smart_list_convert),
            icon = painterResource(R.drawable.ic_smart),
            containerColor = TraktTheme.colors.accent,
            contentColor = Color.White,
            enabled = state.list != null && state.draft != null,
            loading = state.converting.isLoading,
            onClick = onConvertClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                    bottom = bottomInset + 24.dp,
                ),
        )
    }
}

private fun LazyListScope.compareItems(
    items: ImmutableList<SmartListItem>?,
    loading: Boolean,
) {
    if (loading) {
        items(5) {
            PanelMediaSkeletonCard(
                modifier = Modifier.padding(bottom = TraktTheme.spacing.mainListVerticalSpace),
            )
        }
        return
    }

    if (items.isNullOrEmpty()) {
        item(key = "empty") {
            Text(
                text = stringResource(R.string.list_placeholder_empty),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.heading6,
            )
        }
        return
    }

    items(
        items = items,
        key = { it.key },
    ) { item ->
        val itemModifier = Modifier.padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
        when (item) {
            is ShowItem -> ListDetailsShowView(
                show = item.show,
                showUserRating = null,
                sorting = Sorting.Default,
                showIcon = true,
                more = false,
                onLongClick = {},
                modifier = itemModifier,
            )
            is MovieItem -> ListDetailsMovieView(
                movie = item.movie,
                movieUserRating = null,
                sorting = Sorting.Default,
                showIcon = true,
                more = false,
                onLongClick = {},
                modifier = itemModifier,
            )
        }
    }
}

@Composable
private fun TitleBar(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = Modifier
            .height(TraktTheme.size.titleBarHeight)
            .onClick(onClick = onClick)
            .graphicsLayer {
                translationX = -2.dp.toPx()
            },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
        )

        TraktHeader(
            title = title,
            subtitle = subtitle,
            modifier = Modifier.weight(1F, fill = false),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SmartListConvertContent(
            state = SmartListConvertState(),
        )
    }
}
