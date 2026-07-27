package tv.trakt.trakt.app.core.shows

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.chips.FinaleChip
import tv.trakt.trakt.app.common.ui.chips.InfoChip
import tv.trakt.trakt.app.common.ui.chips.PremiereChip
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalViewAllCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.app.core.shows.model.AnticipatedShow
import tv.trakt.trakt.app.core.shows.model.TrendingShow
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

private val sections = listOf(
    "initial",
    "content",
    "trending",
    "releases",
    "hot",
    "popular",
    "anticipated",
)

@Composable
internal fun ShowsScreen(
    viewModel: ShowsViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToTrending: () -> Unit,
    onNavigateToReleases: () -> Unit,
    onNavigateToPopular: () -> Unit,
    onNavigateToAnticipated: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ShowsScreenContent(
        state = state,
        onShowClick = onNavigateToShow,
        onEpisodeClick = onNavigateToEpisode,
        onViewAllTrendingClick = onNavigateToTrending,
        onViewAllReleasesClick = onNavigateToReleases,
        onViewAllPopularClick = onNavigateToPopular,
        onViewAllAnticipatedClick = onNavigateToAnticipated,
    )
}

@Composable
private fun ShowsScreenContent(
    state: ShowsState,
    modifier: Modifier = Modifier,
    onShowClick: (TraktId) -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit = { _, _ -> },
    onViewAllTrendingClick: () -> Unit,
    onViewAllReleasesClick: () -> Unit = {},
    onViewAllPopularClick: () -> Unit,
    onViewAllAnticipatedClick: () -> Unit,
) {
    var focusedShow by remember { mutableStateOf<Show?>(null) }
    var focusedSection by rememberSaveable { mutableStateOf<String?>(null) }

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    LaunchedEffect(state.isLoading, state.trendingShows?.size) {
        if (!state.isLoading && state.trendingShows != null) {
            focusRequesters.getValue("content").requestFocus()
        } else {
            focusRequesters.getValue("initial").requestFocus()
        }
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedSection]?.requestFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedShow?.images?.getFanartUrl(Images.Size.FULL),
            saturation = 0F,
            crossfade = true,
        )

        if (state.error == null) {
            LazyColumn(
                verticalArrangement = spacedBy(TraktTheme.spacing.mainRowVerticalSpace),
                contentPadding = PaddingValues(
                    top = TraktTheme.spacing.mainContentVerticalSpace + 8.dp,
                    bottom = TraktTheme.spacing.mainContentVerticalSpace,
                ),
                modifier = Modifier
                    .focusRequester(focusRequesters.getValue("content")),
            ) {
                item {
                    TrendingShowsList(
                        header = stringResource(R.string.list_title_trending),
                        shows = state.trendingShows,
                        collection = state.collection,
                        isLoading = state.isLoading,
                        onViewAllClick = onViewAllTrendingClick,
                        onShowClick = onShowClick,
                        onShowFocus = {
                            focusedShow = it
                            focusedSection = "trending"
                        },
                        focusRequesters = focusRequesters,
                        modifier = Modifier
                            .focusGroup()
                            .focusRequester(focusRequesters.getValue("trending")),
                    )
                }

                item {
                    ReleasesShowsList(
                        header = stringResource(R.string.list_title_releases),
                        episodes = state.releasesShows,
                        isLoading = state.isLoading,
                        onViewAllClick = onViewAllReleasesClick,
                        onClick = { item -> onEpisodeClick(item.show.ids.trakt, item.episode) },
                        onFocused = {
                            focusedShow = it.show
                            focusedSection = "releases"
                        },
                        focusRequesters = focusRequesters,
                        modifier = Modifier
                            .focusGroup()
                            .focusRequester(focusRequesters.getValue("releases")),
                    )
                }

                item {
                    AnticipatedShowsList(
                        header = stringResource(R.string.list_title_most_anticipated),
                        shows = state.anticipatedShows,
                        collection = state.collection,
                        isLoading = state.isLoading,
                        onViewAllClick = onViewAllAnticipatedClick,
                        onShowClick = onShowClick,
                        onFocusedShow = {
                            focusedShow = it
                            focusedSection = "anticipated"
                        },
                        modifier = Modifier
                            .focusGroup()
                            .focusRequester(focusRequesters.getValue("anticipated")),
                    )
                }

                item {
                    PopularShowsList(
                        header = stringResource(R.string.list_title_most_popular),
                        shows = state.popularShows,
                        collection = state.collection,
                        isLoading = state.isLoading,
                        onViewAllClick = onViewAllPopularClick,
                        onShowClick = onShowClick,
                        onShowFocus = {
                            focusedShow = it
                            focusedSection = "popular"
                        },
                        modifier = Modifier
                            .focusGroup()
                            .focusRequester(focusRequesters.getValue("popular")),
                    )
                }
            }
        }

        if (state.error != null) {
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

@Composable
private fun TrendingShowsList(
    header: String,
    shows: ImmutableList<TrendingShow>?,
    collection: UserCollectionState,
    isLoading: Boolean,
    onShowFocus: (Show) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onViewAllClick: () -> Unit,
    focusRequesters: Map<String, FocusRequester>,
    modifier: Modifier = Modifier,
) {
    var isFocusable by rememberSaveable { mutableStateOf(true) }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier
                .padding(start = TraktTheme.spacing.mainContentStartSpace)
                .focusRequester(focusRequesters.getValue("initial"))
                .focusable(isFocusable)
                .onFocusChanged { isFocusable = false },
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = 32.dp,
            ),
        ) {
            if (isLoading) {
                items(count = 10) {
                    HorizontalMediaSkeletonCard()
                }
            } else if (!shows.isNullOrEmpty()) {
                items(
                    items = shows,
                    key = { item -> item.show.ids.trakt.value },
                ) { (watchers, show) ->
                    HorizontalMediaCard(
                        title = show.title,
                        watched = collection.isWatched(show.ids.trakt, MediaType.Show, show.airedEpisodes),
                        watching = collection.isWatching(show.ids.trakt, MediaType.Show, show.airedEpisodes),
                        watchlist = collection.isWatchlist(show.ids.trakt, MediaType.Show),
                        onClick = { onShowClick(show.ids.trakt) },
                        containerImageUrl = show.images?.getFanartUrl(),
                        contentImageUrl = show.images?.getLogoUrl(),
                        paletteColor = show.colors?.colors?.second,
                        footerContent = {
                            Column(
                                verticalArrangement = spacedBy(1.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = spacedBy(5.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_person_double),
                                        contentDescription = null,
                                        tint = TraktTheme.colors.textPrimary,
                                        modifier = Modifier.size(12.dp),
                                    )
                                    Text(
                                        text = rememberThousandsFormat(watchers),
                                        style = TraktTheme.typography.cardTitle,
                                        color = TraktTheme.colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        },
                        modifier = Modifier.onFocusChanged {
                            if (it.hasFocus) {
                                onShowFocus(show)
                            }
                        },
                    )
                }

                item {
                    HorizontalViewAllCard(
                        onClick = onViewAllClick,
                    )
                }

                emptyFocusListItems()
            }
        }
    }
}

@Composable
private fun ReleasesShowsList(
    header: String,
    episodes: ImmutableList<HomeUpcomingItem.EpisodeItem>?,
    isLoading: Boolean,
    onFocused: (HomeUpcomingItem.EpisodeItem) -> Unit,
    onClick: (HomeUpcomingItem.EpisodeItem) -> Unit,
    onViewAllClick: () -> Unit,
    focusRequesters: Map<String, FocusRequester>,
    modifier: Modifier = Modifier,
) {
    var isFocusable by rememberSaveable { mutableStateOf(true) }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier
                .padding(start = TraktTheme.spacing.mainContentStartSpace)
                .focusRequester(focusRequesters.getValue("initial"))
                .focusable(isFocusable)
                .onFocusChanged { isFocusable = false },
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = 32.dp,
            ),
        ) {
            if (isLoading) {
                items(count = 10) {
                    HorizontalMediaSkeletonCard()
                }
            } else if (!episodes.isNullOrEmpty()) {
                items(
                    items = episodes,
                    key = { item -> item.show.ids.trakt.value },
                ) { item ->
                    HorizontalMediaCard(
                        title = "",
                        containerImageUrl = item.images?.getFanartUrl(),
                        onClick = { onClick(item) },
                        cardContent = {
                            Column(
                                verticalArrangement = spacedBy(2.dp),
                            ) {
                                when {
                                    item.episode.isPremiere() -> PremiereChip()
                                    item.episode.isFinale() -> FinaleChip()
                                }

                                InfoChip(
                                    text = item.releaseAt?.toLocal()?.relativeDateTimeString() ?: "TBA",
                                    iconPainter = when {
                                        item.episode.isReleased -> painterResource(R.drawable.ic_calendar_check)
                                        else -> painterResource(R.drawable.ic_calendar_upcoming)
                                    },
                                    containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F),
                                )
                            }
                        },
                        footerContent = {
                            Column(
                                verticalArrangement = spacedBy(1.dp),
                            ) {
                                Text(
                                    text = item.title,
                                    style = TraktTheme.typography.cardTitle,
                                    color = TraktTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                val subtitle = when {
                                    item.isFullSeason -> stringResource(
                                        R.string.text_season_number,
                                        item.episode.season,
                                    )
                                    item.episodes.size > 1 -> stringResource(
                                        R.string.episode_footer_season_episode_range,
                                        item.episode.season,
                                        item.episodes.first().number,
                                        item.episodes.last().number,
                                    )
                                    else -> item.episode.seasonEpisodeString()
                                }

                                Text(
                                    text = subtitle,
                                    style = TraktTheme.typography.cardSubtitle,
                                    color = TraktTheme.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        modifier = Modifier
                            .onFocusChanged {
                                if (it.isFocused) {
                                    onFocused(item)
                                }
                            },
                    )
                }

                item {
                    HorizontalViewAllCard(
                        onClick = onViewAllClick,
                    )
                }

                emptyFocusListItems()
            }
        }
    }
}

@Composable
private fun AnticipatedShowsList(
    header: String,
    shows: List<AnticipatedShow>?,
    collection: UserCollectionState,
    isLoading: Boolean,
    onFocusedShow: (Show) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(start = TraktTheme.spacing.mainContentStartSpace),
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = 32.dp,
            ),
        ) {
            if (isLoading) {
                items(count = 10) {
                    HorizontalMediaSkeletonCard(
                        modifier = Modifier
                            .focusProperties { canFocus = false },
                    )
                }
            } else if (!shows.isNullOrEmpty()) {
                items(
                    items = shows,
                    key = { item -> item.show.ids.trakt.value },
                ) { (listCount, show) ->
                    HorizontalMediaCard(
                        title = show.title,
                        watched = collection.isWatched(show.ids.trakt, MediaType.Show, show.airedEpisodes),
                        watching = collection.isWatching(show.ids.trakt, MediaType.Show, show.airedEpisodes),
                        watchlist = collection.isWatchlist(show.ids.trakt, MediaType.Show),
                        onClick = { onShowClick(show.ids.trakt) },
                        containerImageUrl = show.images?.getFanartUrl(),
                        contentImageUrl = show.images?.getLogoUrl(),
                        paletteColor = show.colors?.colors?.second,
                        footerContent = {
                            Column(
                                verticalArrangement = spacedBy(1.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = spacedBy(2.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_bookmark_off),
                                        contentDescription = null,
                                        tint = TraktTheme.colors.textPrimary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = rememberThousandsFormat(listCount),
                                        style = TraktTheme.typography.cardTitle,
                                        color = TraktTheme.colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        },
                        modifier = Modifier.onFocusChanged {
                            if (it.hasFocus) {
                                onFocusedShow(show)
                            }
                        },
                    )
                }

                item {
                    HorizontalViewAllCard(
                        onClick = onViewAllClick,
                    )
                }

                emptyFocusListItems()
            }
        }
    }
}

@Composable
private fun PopularShowsList(
    header: String,
    shows: ImmutableList<Show>?,
    collection: UserCollectionState,
    isLoading: Boolean,
    onShowFocus: (Show) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(start = TraktTheme.spacing.mainContentStartSpace),
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = 32.dp,
            ),
        ) {
            if (isLoading) {
                items(count = 10) {
                    HorizontalMediaSkeletonCard()
                }
            } else if (!shows.isNullOrEmpty()) {
                items(
                    items = shows,
                    key = { item -> item.ids.trakt.value },
                ) { show ->
                    HorizontalMediaCard(
                        title = show.title,
                        watched = collection.isWatched(show.ids.trakt, MediaType.Show, show.airedEpisodes),
                        watching = collection.isWatching(show.ids.trakt, MediaType.Show, show.airedEpisodes),
                        watchlist = collection.isWatchlist(show.ids.trakt, MediaType.Show),
                        onClick = { onShowClick(show.ids.trakt) },
                        containerImageUrl = show.images?.getFanartUrl(),
                        contentImageUrl = show.images?.getLogoUrl(),
                        paletteColor = show.colors?.colors?.second,
                        footerContent = {
                            Column(
                                verticalArrangement = spacedBy(1.dp),
                            ) {
                                val episodes =
                                    stringResource(R.string.tag_text_number_of_episodes, show.airedEpisodes)
                                Text(
                                    text = show.year?.let { "$it  •  $episodes" } ?: episodes,
                                    style = TraktTheme.typography.cardTitle,
                                    color = TraktTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        modifier = Modifier.onFocusChanged {
                            if (it.hasFocus) {
                                onShowFocus(show)
                            }
                        },
                    )
                }

                item {
                    HorizontalViewAllCard(
                        onClick = onViewAllClick,
                    )
                }

                emptyFocusListItems()
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
        ShowsScreenContent(
            state = ShowsState(
                trendingShows = listOf(
                    TrendingShow(
                        watchers = 12341,
                        show = PreviewData.show1,
                    ),
                    TrendingShow(
                        watchers = 872,
                        show = PreviewData.show2,
                    ),
                ).toImmutableList(),
            ),
            onShowClick = {},
            onViewAllTrendingClick = {},
            onViewAllPopularClick = {},
            onViewAllAnticipatedClick = {},
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
        ShowsScreenContent(
            state = ShowsState(
                isLoading = true,
                trendingShows = listOf(
                    TrendingShow(
                        watchers = 12341,
                        show = PreviewData.show1,
                    ),
                    TrendingShow(
                        watchers = 872,
                        show = PreviewData.show2,
                    ),
                ).toImmutableList(),
            ),
            onShowClick = {},
            onViewAllTrendingClick = {},
            onViewAllPopularClick = {},
            onViewAllAnticipatedClick = {},
        )
    }
}
