@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.home.sections.upnext

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_EMPTY_IMAGE_1
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upnext.HomeUpNextState.ItemsState
import tv.trakt.trakt.core.home.sections.upnext.features.context.sheets.UpNextItemContextSheet
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextItem
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextMovie
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextShow
import tv.trakt.trakt.core.home.sections.upnext.ui.HomeUpNextMovieView
import tv.trakt.trakt.core.home.sections.upnext.ui.HomeUpNextShowView
import tv.trakt.trakt.core.home.views.HomeEmptyView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
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
    onMovieClick: (TraktId) -> Unit,
    onShowsClick: () -> Unit,
    onMoviesClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var contextSheet by remember { mutableStateOf<UpNextItem?>(null) }
    var dateSheet by remember { mutableStateOf<UpNextShow?>(null) }

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
        onCollapse = viewModel::setCollapsed,
        onShowsClick = onShowsClick,
        onMoviesClick = onMoviesClick,
        onShowClick = { onShowClick(it.show.ids.trakt) },
        onMovieClick = { onMovieClick(it.movie.ids.trakt) },
        onMoreClick = {
            if (state.loading == Done && !state.items.items.isNullOrEmpty()) {
                onMoreClick()
            }
        },
        onClick = {
            if (it.loading) {
                return@HomeUpNextContent
            }
            when (it) {
                is UpNextShow -> it.progress.nextEpisode?.let { episode ->
                    onEpisodeClick(it.show.ids.trakt, episode)
                }
                is UpNextMovie -> onMovieClick(it.movie.ids.trakt)
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
            if (!it.loading && it is UpNextShow) {
                dateSheet = it
            }
        },
    )

    UpNextItemContextSheet(
        sheetItem = contextSheet,
        onDismiss = { contextSheet = null },
        onAddWatched = {
            dateSheet = it as UpNextShow
        },
        onDropped = {
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
        onCheckIn = {
            val item = dateSheet ?: return@HomeDateSelectionSheet
            val episode = item.progress.nextEpisode ?: return@HomeDateSelectionSheet
            viewModel.addEpisodeCheckIn(
                showId = item.show.ids.trakt,
                episodeId = episode.ids.trakt,
                seasonEpisode = episode.seasonEpisode,
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
    onMoviesClick: () -> Unit = {},
    onShowClick: (UpNextShow) -> Unit = {},
    onMovieClick: (UpNextMovie) -> Unit = {},
    onClick: (UpNextItem) -> Unit = {},
    onLongClick: (UpNextItem) -> Unit = {},
    onCheckClick: (UpNextItem) -> Unit = {},
    onCheckLongClick: (UpNextItem) -> Unit = {},
    onCollapse: (collapsed: Boolean) -> Unit = {},
) {
    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier
            .animateContentSize(
                animationSpec = if (animateCollapse) spring() else snap(),
            ),
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.list_title_up_next),
            chevron = !state.items.items.isNullOrEmpty() || state.loading != Done,
            collapsed = state.collapsed ?: false,
            onCollapseClick = {
                animateCollapse = true
                val current = (state.collapsed ?: false)
                onCollapse(!current)
            },
            modifier = Modifier
                .padding(headerPadding)
                .onClick(enabled = state.loading == Done) {
                    onMoreClick()
                },
        )

        if (state.collapsed != true) {
            Crossfade(
                targetState = state.loading,
                animationSpec = tween(200),
            ) { loading ->
                when (loading) {
                    Idle, Loading -> {
                        ContentLoadingList(
                            visible = loading.isLoading,
                            contentPadding = contentPadding,
                        )
                    }

                    Done -> {
                        when {
                            state.error != null -> {
                                Text(
                                    text =
                                        "${
                                            stringResource(
                                                R.string.error_text_unexpected_error_short,
                                            )
                                        }\n\n${state.error}",
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
                                    text = stringResource(
                                        when (state.filter?.mode) {
                                            MediaMode.MOVIES -> R.string.text_cta_up_next_movies
                                            else -> R.string.text_cta_up_next
                                        },
                                    ),
                                    icon = R.drawable.ic_empty_upnext,
                                    buttonText = when (state.filter?.mode) {
                                        MediaMode.MOVIES -> stringResource(R.string.link_text_discover_movies)
                                        else -> stringResource(R.string.link_text_discover_shows)
                                    },
                                    backgroundImageUrl = imageUrl,
                                    backgroundImage = if (imageUrl == null) R.drawable.ic_splash_background_2 else null,
                                    modifier = Modifier.padding(contentPadding),
                                    onClick = {
                                        when (state.filter?.mode) {
                                            MediaMode.MOVIES -> onMoviesClick()
                                            else -> onShowsClick()
                                        }
                                    },
                                )
                            }

                            else -> {
                                ContentList(
                                    listFilter = state.filter?.mode,
                                    listItems = state.items,
                                    contentPadding = contentPadding,
                                    onClick = onClick,
                                    onLongClick = onLongClick,
                                    onCheckClick = onCheckClick,
                                    onCheckLongClick = onCheckLongClick,
                                    onShowClick = onShowClick,
                                    onMovieClick = onMovieClick,
                                )
                            }
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
        items(count = 8) {
            EpisodeSkeletonCard()
        }
    }
}

@Composable
private fun ContentList(
    listFilter: MediaMode?,
    listItems: ItemsState,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    onClick: (UpNextItem) -> Unit,
    onLongClick: (UpNextItem) -> Unit,
    onCheckClick: (UpNextItem) -> Unit,
    onCheckLongClick: (UpNextItem) -> Unit,
    onShowClick: (UpNextShow) -> Unit,
    onMovieClick: (UpNextMovie) -> Unit,
) {
    val currentFilter = remember { mutableStateOf(listFilter) }
    val listHash = rememberSaveable { mutableIntStateOf(listItems.items.hashCode()) }

    LaunchedEffect(listFilter) {
        if (currentFilter.value != listFilter) {
            currentFilter.value = listFilter
            listState.animateScrollToItem(0)
        }
    }

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
            if (item is UpNextShow) {
                HomeUpNextShowView(
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
            } else if (item is UpNextMovie) {
                HomeUpNextMovieView(
                    item = item,
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    onMovieClick = { onMovieClick(item) },
                    modifier = Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                    ),
                )
            }
        }
    }
}

@Composable
private fun HomeDateSelectionSheet(
    item: UpNextShow?,
    onDateSelected: (DateSelectionResult?) -> Unit,
    onCheckIn: () -> Unit,
    onDismiss: () -> Unit,
) {
    DateSelectionSheet(
        active = item != null,
        title = item?.show?.title.orEmpty(),
        subtitle = item?.progress?.nextEpisode?.seasonEpisodeString(),
        nowWatchingVisible = true,
        onResult = {
            if (item == null) return@DateSelectionSheet
            onDateSelected(it)
        },
        onCheckIn = onCheckIn,
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
                loading = Idle,
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
                loading = Loading,
            ),
        )
    }
}
