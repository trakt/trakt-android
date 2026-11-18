package tv.trakt.trakt.app.core.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.chips.MediaModeFilters
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.discover.model.DiscoverMediaSection
import tv.trakt.trakt.app.core.discover.views.DiscoverItemsRow
import tv.trakt.trakt.app.core.discover.views.DiscoverReleasesRow
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.discover.DiscoverItem
import tv.trakt.trakt.resources.R

private const val SECTION_TRENDING = "trending"
private const val SECTION_RELEASES = "releases"
private const val SECTION_ANTICIPATED = "anticipated"
private const val SECTION_POPULAR = "popular"

private val directionKeys = setOf(
    Key.DirectionUp,
    Key.DirectionDown,
    Key.DirectionLeft,
    Key.DirectionRight,
)

private val sections = listOf(
    SECTION_TRENDING,
    SECTION_RELEASES,
    SECTION_ANTICIPATED,
    SECTION_POPULAR,
)

@Composable
internal fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToTrending: (MediaMode) -> Unit,
    onNavigateToReleases: (MediaMode) -> Unit,
    onNavigateToPopular: (MediaMode) -> Unit,
    onNavigateToAnticipated: (MediaMode) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DiscoverScreenContent(
        state = state,
        onModeClick = viewModel::setMode,
        onShowClick = onNavigateToShow,
        onMovieClick = onNavigateToMovie,
        onEpisodeClick = onNavigateToEpisode,
        onViewAllTrendingClick = { onNavigateToTrending(state.mode) },
        onViewAllReleasesClick = { onNavigateToReleases(state.mode) },
        onViewAllPopularClick = { onNavigateToPopular(state.mode) },
        onViewAllAnticipatedClick = { onNavigateToAnticipated(state.mode) },
    )
}

@Composable
private fun DiscoverScreenContent(
    state: DiscoverState,
    modifier: Modifier = Modifier,
    onModeClick: (MediaMode) -> Unit = {},
    onShowClick: (TraktId) -> Unit = {},
    onMovieClick: (TraktId) -> Unit = {},
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit = { _, _ -> },
    onViewAllTrendingClick: () -> Unit = {},
    onViewAllReleasesClick: () -> Unit = {},
    onViewAllPopularClick: () -> Unit = {},
    onViewAllAnticipatedClick: () -> Unit = {},
) {
    var focusedImages by remember { mutableStateOf<Images?>(null) }
    var focusedSection by rememberSaveable { mutableStateOf<String?>(null) }

    val focusRequesters = remember {
        sections.associateWith { FocusRequester() }
    }
    val filtersFocusRequester = remember { FocusRequester() }

    var focusTrendingOnLoad by remember { mutableStateOf(focusedSection == null) }

    val loadingFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        val requester = focusedSection?.let { focusRequesters[it] } ?: loadingFocusRequester
        withFrameNanos { }
        requester.requestSafeFocus()
    }

    LaunchedEffect(state.isLoading, state.trending) {
        if (!focusTrendingOnLoad || state.isLoading) {
            return@LaunchedEffect
        }
        withFrameNanos { }
        when {
            state.trending.isNullOrEmpty() -> filtersFocusRequester.requestSafeFocus()
            else -> focusRequesters.getValue(SECTION_TRENDING).requestSafeFocus()
        }
        focusTrendingOnLoad = false
    }

    val onItemClick: (DiscoverItem) -> Unit = { item ->
        when (item) {
            is DiscoverItem.ShowItem -> onShowClick(item.id)
            is DiscoverItem.MovieItem -> onMovieClick(item.id)
        }
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyDown && it.key in directionKeys) {
                    focusTrendingOnLoad = false
                }
                false
            }
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedSection]?.requestSafeFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = (focusedImages ?: state.trending?.firstOrNull()?.images)?.getFanartUrl(Size.FULL),
            saturation = 0F,
            crossfade = true,
        )

        val filters = @Composable {
            MediaModeFilters(
                selectedMode = state.mode,
                selectedFocusRequester = filtersFocusRequester,
                downFocusRequester = focusRequesters.getValue(SECTION_TRENDING),
                onModeClick = { mode ->
                    focusTrendingOnLoad = false
                    focusedSection = null
                    focusedImages = null
                    onModeClick(mode)
                },
                modifier = Modifier.padding(start = TraktTheme.spacing.mainContentStartSpace),
            )
        }

        if (state.error == null) {
            LazyColumn(
                verticalArrangement = spacedBy(TraktTheme.spacing.mainRowVerticalSpace),
                contentPadding = PaddingValues(
                    top = TraktTheme.spacing.mainContentVerticalSpace + 8.dp,
                    bottom = TraktTheme.spacing.mainContentVerticalSpace,
                ),
                modifier = Modifier.focusGroup(),
            ) {
                item {
                    filters()
                }

                item {
                    Box {
                        DiscoverItemsRow(
                            header = stringResource(DiscoverMediaSection.Trending.titleRes),
                            section = DiscoverMediaSection.Trending,
                            items = state.trending,
                            collection = state.collection,
                            isLoading = state.isLoading,
                            onItemClick = onItemClick,
                            onItemFocus = {
                                focusedImages = it.images
                                focusedSection = SECTION_TRENDING
                            },
                            onViewAllClick = onViewAllTrendingClick,
                            modifier = Modifier
                                .focusGroup()
                                .focusRequester(focusRequesters.getValue(SECTION_TRENDING)),
                        )

                        if (focusTrendingOnLoad) {
                            Box(
                                modifier = Modifier
                                    .size(1.dp)
                                    .focusRequester(loadingFocusRequester)
                                    .focusable(),
                            )
                        }
                    }
                }

                item {
                    DiscoverReleasesRow(
                        header = stringResource(R.string.list_title_releases),
                        items = state.releases,
                        isLoading = state.isLoading,
                        onItemClick = { item ->
                            when (item) {
                                is CalendarItem.EpisodeItem -> onEpisodeClick(item.show.ids.trakt, item.episode)
                                is CalendarItem.MovieItem -> onMovieClick(item.movie.ids.trakt)
                            }
                        },
                        onItemFocus = {
                            focusedImages = it.images
                            focusedSection = SECTION_RELEASES
                        },
                        onViewAllClick = onViewAllReleasesClick,
                        modifier = Modifier
                            .focusGroup()
                            .focusRequester(focusRequesters.getValue(SECTION_RELEASES)),
                    )
                }

                item {
                    DiscoverItemsRow(
                        header = stringResource(DiscoverMediaSection.Anticipated.titleRes),
                        section = DiscoverMediaSection.Anticipated,
                        items = state.anticipated,
                        collection = state.collection,
                        isLoading = state.isLoading,
                        onItemClick = onItemClick,
                        onItemFocus = {
                            focusedImages = it.images
                            focusedSection = SECTION_ANTICIPATED
                        },
                        onViewAllClick = onViewAllAnticipatedClick,
                        modifier = Modifier
                            .focusGroup()
                            .focusRequester(focusRequesters.getValue(SECTION_ANTICIPATED)),
                    )
                }

                item {
                    DiscoverItemsRow(
                        header = stringResource(DiscoverMediaSection.Popular.titleRes),
                        section = DiscoverMediaSection.Popular,
                        items = state.popular,
                        collection = state.collection,
                        isLoading = state.isLoading,
                        onItemClick = onItemClick,
                        onItemFocus = {
                            focusedImages = it.images
                            focusedSection = SECTION_POPULAR
                        },
                        onViewAllClick = onViewAllPopularClick,
                        modifier = Modifier
                            .focusGroup()
                            .focusRequester(focusRequesters.getValue(SECTION_POPULAR)),
                    )
                }
            }
        } else {
            Column {
                Box(modifier = Modifier.padding(top = TraktTheme.spacing.mainContentVerticalSpace + 8.dp)) {
                    filters()
                }

                GenericErrorView(
                    error = state.error,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = TraktTheme.spacing.mainContentStartSpace,
                            end = TraktTheme.spacing.mainContentEndSpace,
                        ),
                )
            }
        }
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        DiscoverScreenContent(
            state = DiscoverState(
                isLoading = false,
                trending = listOf(
                    DiscoverItem.MovieItem(count = 12341, movie = PreviewData.movie1),
                    DiscoverItem.MovieItem(count = 872, movie = PreviewData.movie2),
                ).toImmutableList(),
            ),
        )
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        DiscoverScreenContent(
            state = DiscoverState(
                mode = MediaMode.Shows,
                isLoading = true,
            ),
        )
    }
}
