package tv.trakt.trakt.app.core.shows

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalViewAllCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.shows.model.AnticipatedShow
import tv.trakt.trakt.app.core.shows.model.TrendingShow
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.helpers.preview.PreviewData
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.thousandsFormat
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.R as RCommon

private val sections = listOf(
    "initial",
    "content",
    "trending",
    "hot",
    "popular",
    "anticipated",
    "recommended",
)

@Composable
internal fun ShowsScreen(
    viewModel: ShowsViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToTrending: () -> Unit,
    onNavigateToPopular: () -> Unit,
    onNavigateToAnticipated: () -> Unit,
    onNavigateToRecommended: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ShowsScreenContent(
        state = state,
        onShowClick = onNavigateToShow,
        onViewAllTrendingClick = onNavigateToTrending,
        onViewAllPopularClick = onNavigateToPopular,
        onViewAllAnticipatedClick = onNavigateToAnticipated,
        onViewAllRecommendedClick = onNavigateToRecommended,
    )
}

@Composable
private fun ShowsScreenContent(
    state: ShowsState,
    modifier: Modifier = Modifier,
    onShowClick: (TraktId) -> Unit,
    onViewAllTrendingClick: () -> Unit,
    onViewAllPopularClick: () -> Unit,
    onViewAllAnticipatedClick: () -> Unit,
    onViewAllRecommendedClick: () -> Unit,
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
                        header = stringResource(RCommon.string.header_trending),
                        shows = state.trendingShows,
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
                    AnticipatedShowsList(
                        header = stringResource(RCommon.string.header_most_anticipated),
                        shows = state.anticipatedShows,
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
                        header = stringResource(RCommon.string.header_most_popular),
                        shows = state.popularShows,
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

                if (state.recommendedShows != null) {
                    item {
                        RecommendedShowsList(
                            header = stringResource(RCommon.string.header_recommended),
                            shows = state.recommendedShows,
                            isLoading = state.isLoading,
                            onViewAllClick = onViewAllRecommendedClick,
                            onShowClick = onShowClick,
                            onShowFocus = {
                                focusedShow = it
                                focusedSection = "recommended"
                            },
                            modifier = Modifier
                                .focusGroup()
                                .focusRequester(focusRequesters.getValue("recommended")),
                        )
                    }
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
                        onClick = { onShowClick(show.ids.trakt) },
                        containerImageUrl = show.images?.getFanartUrl(),
                        contentImageUrl = show.images?.getLogoUrl(),
                        paletteColor = show.colors?.colors?.second,
                        footerContent = {
                            InfoChip(
                                text = stringResource(RCommon.string.people_watching, watchers.thousandsFormat()),
                                modifier = Modifier,
                            )
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
private fun RecommendedShowsList(
    header: String,
    shows: ImmutableList<Show>?,
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
                    HorizontalMediaSkeletonCard(
                        modifier = Modifier
                            .focusProperties { canFocus = false },
                    )
                }
            } else if (!shows.isNullOrEmpty()) {
                items(
                    items = shows,
                    key = { item -> item.ids.trakt.value },
                ) { show ->
                    HorizontalMediaCard(
                        title = show.title,
                        onClick = { onShowClick(show.ids.trakt) },
                        containerImageUrl = show.images?.getFanartUrl(),
                        contentImageUrl = show.images?.getLogoUrl(),
                        paletteColor = show.colors?.colors?.second,
                        footerContent = {
                            InfoChip(
                                text = stringResource(RCommon.string.episodes_count, show.airedEpisodes),
                            )
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
private fun AnticipatedShowsList(
    header: String,
    shows: List<AnticipatedShow>?,
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
                        onClick = { onShowClick(show.ids.trakt) },
                        containerImageUrl = show.images?.getFanartUrl(),
                        contentImageUrl = show.images?.getLogoUrl(),
                        paletteColor = show.colors?.colors?.second,
                        footerContent = {
                            InfoChip(
                                text = stringResource(RCommon.string.people_eager, listCount.thousandsFormat()),
                            )
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
                        onClick = { onShowClick(show.ids.trakt) },
                        containerImageUrl = show.images?.getFanartUrl(),
                        contentImageUrl = show.images?.getLogoUrl(),
                        paletteColor = show.colors?.colors?.second,
                        footerContent = {
                            InfoChip(
                                text = stringResource(RCommon.string.episodes_count, show.airedEpisodes),
                            )
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
            onViewAllRecommendedClick = {},
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
            onViewAllRecommendedClick = {},
        )
    }
}
