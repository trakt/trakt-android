@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.home.sections.upnext

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_1
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.home.sections.upnext.HomeUpNextState.ItemsState
import tv.trakt.trakt.core.home.sections.upnext.features.context.sheets.UpNextItemContextSheet
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow
import tv.trakt.trakt.core.home.views.HomeEmptyView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EpisodeProgressBar
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HomeUpNextView(
    modifier: Modifier = Modifier,
    viewModel: HomeUpNextViewModel = koinViewModel(),
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onShowsClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var contextSheet by remember { mutableStateOf<ProgressShow?>(null) }
    var dateSheet by remember { mutableStateOf<ProgressShow?>(null) }

    LaunchedEffect(state.info) {
        if (state.info != null) {
            haptic.performHapticFeedback(Confirm)
            viewModel.clearInfo()
        }
    }

    HomeUpNextContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onShowsClick = onShowsClick,
        onShowClick = { onShowClick(it.show.ids.trakt) },
        onMoreClick = {
            if (state.loading == DONE) {
                onMoreClick()
            }
        },
        onClick = {
            if (!it.loading) {
                onEpisodeClick(
                    it.show.ids.trakt,
                    it.progress.nextEpisode,
                )
            }
        },
        onLongClick = {
            if (!it.loading) {
                contextSheet = it
            }
        },
        onCheckClick = {
            viewModel.addToHistory(it.id)
        },
        onCheckLongClick = {
            dateSheet = it
        },
    )

    UpNextItemContextSheet(
        sheetItem = contextSheet,
        onDismiss = { contextSheet = null },
        onAddWatched = {
            dateSheet = it
        },
        onDropShow = {
            viewModel.loadData(
                resetScroll = false,
                ignoreErrors = false,
            )
        },
    )

    HomeDateSelectionSheet(
        item = dateSheet,
        onDateSelected = { date ->
            val episode = dateSheet?.progress?.nextEpisode
                ?: return@HomeDateSelectionSheet

            viewModel.addToHistory(
                episodeId = episode.ids.trakt,
                customDate = date,
            )
        },
        onDismiss = {
            dateSheet = null
        },
    )
}

@Composable
internal fun HomeUpNextContent(
    state: HomeUpNextState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onMoreClick: () -> Unit = {},
    onShowsClick: () -> Unit = {},
    onShowClick: (ProgressShow) -> Unit = {},
    onClick: (ProgressShow) -> Unit = {},
    onLongClick: (ProgressShow) -> Unit = {},
    onCheckClick: (ProgressShow) -> Unit = {},
    onCheckLongClick: (ProgressShow) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding)
                .onClick(enabled = state.loading == DONE) {
                    onMoreClick()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.list_title_up_next),
            )
            if (!state.items.items.isNullOrEmpty() || state.loading != DONE) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            translationX = (4.9).dp.toPx()
                        },
                )
            }
        }

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ContentLoadingList(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
                    when {
                        state.error != null -> {
                            Text(
                                text =
                                    "${stringResource(R.string.error_text_unexpected_error_short)}\n\n${state.error}",
                                color = TraktTheme.colors.textSecondary,
                                style = TraktTheme.typography.meta,
                                maxLines = 10,
                                modifier = Modifier.padding(contentPadding),
                            )
                        }
                        state.items.items?.isEmpty() == true -> {
                            val imageUrl = remember {
                                Firebase.remoteConfig.getString(MOBILE_EMPTY_IMAGE_1).ifBlank { null }
                            }
                            HomeEmptyView(
                                text = stringResource(R.string.text_cta_up_next),
                                icon = R.drawable.ic_empty_upnext,
                                buttonText = stringResource(R.string.button_label_browse_shows),
                                backgroundImageUrl = imageUrl,
                                backgroundImage = if (imageUrl == null) R.drawable.ic_splash_background_2 else null,
                                modifier = Modifier.padding(contentPadding),
                                onClick = onShowsClick,
                            )
                        }
                        else -> {
                            ContentList(
                                listItems = state.items,
                                contentPadding = contentPadding,
                                onClick = onClick,
                                onLongClick = onLongClick,
                                onCheckClick = onCheckClick,
                                onCheckLongClick = onCheckLongClick,
                                onShowClick = onShowClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentLoadingList(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 6) {
            EpisodeSkeletonCard()
        }
    }
}

@Composable
private fun ContentList(
    listItems: ItemsState,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onClick: (ProgressShow) -> Unit,
    onLongClick: (ProgressShow) -> Unit,
    onCheckClick: (ProgressShow) -> Unit,
    onCheckLongClick: (ProgressShow) -> Unit,
    onShowClick: (ProgressShow) -> Unit,
) {
    val listHash = rememberSaveable { mutableIntStateOf(listItems.items.hashCode()) }

    LaunchedEffect(listItems.items, listItems.resetScroll) {
        val hash = listItems.hashCode()
        if (listHash.intValue != hash) {
            listHash.intValue = hash
            if (listItems.resetScroll) {
                listState.animateScrollToItem(0)
            }
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems.items ?: emptyList(),
            key = { it.key },
        ) { item ->
            ContentListItem(
                item = item,
                onClick = { onClick(item) },
                onLongClick = { onLongClick(item) },
                onCheckClick = { onCheckClick(item) },
                onCheckLongClick = { onCheckLongClick(item) },
                onShowClick = { onShowClick(item) },
                modifier = Modifier.animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                ),
            )
        }
    }
}

@Composable
private fun ContentListItem(
    item: ProgressShow,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckClick: () -> Unit,
    onCheckLongClick: () -> Unit,
    onShowClick: () -> Unit,
) {
    HorizontalMediaCard(
        title = "",
        onClick = onClick,
        onLongClick = onLongClick,
        containerImageUrl =
            item.progress.nextEpisode.images?.getScreenshotUrl()
                ?: item.show.images?.getFanartUrl(),
        cardContent = {
            Row {
                val runtime = item.progress.nextEpisode.runtime?.inWholeMinutes
                    ?: item.show.runtime?.inWholeMinutes

                val remainingEpisodes = remember(item.progress.completed, item.progress.aired) {
                    item.progress.remainingEpisodes
                }

                val remainingPercent = remember(item.progress.completed, item.progress.aired) {
                    item.progress.remainingPercent
                }

                EpisodeProgressBar(
                    startText = runtime?.durationFormat(),
                    endText = stringResource(R.string.tag_text_remaining_episodes, remainingEpisodes),
                    progress = remainingPercent,
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(100)),
                )
            }
        },
        footerContent = {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = spacedBy(1.dp),
                    modifier = Modifier
                        .onClick(onClick = onShowClick)
                        .weight(1F, fill = false),
                ) {
                    Text(
                        text = item.show.title,
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = item.progress.nextEpisode.seasonEpisodeString(),
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                val checkSize = 20.dp
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(start = 12.dp, end = 4.dp)
                        .size(checkSize),
                ) {
                    if (item.loading) {
                        FilmProgressIndicator(size = checkSize - 3.dp)
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = TraktTheme.colors.accent,
                            modifier = Modifier
                                .size(checkSize)
                                .onClickCombined(
                                    onClick = onCheckClick,
                                    onLongClick = onCheckLongClick,
                                ),
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun HomeDateSelectionSheet(
    item: ProgressShow?,
    onDateSelected: (DateSelectionResult?) -> Unit,
    onDismiss: () -> Unit,
) {
    DateSelectionSheet(
        active = item != null,
        title = item?.show?.title.orEmpty(),
        subtitle = item?.progress?.nextEpisode?.seasonEpisodeString(),
        onResult = {
            if (item == null) return@DateSelectionSheet
            onDateSelected(it)
        },
        onDismiss = onDismiss,
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        HomeUpNextContent(
            state = HomeUpNextState(
                loading = IDLE,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        HomeUpNextContent(
            state = HomeUpNextState(
                loading = LOADING,
            ),
        )
    }
}
