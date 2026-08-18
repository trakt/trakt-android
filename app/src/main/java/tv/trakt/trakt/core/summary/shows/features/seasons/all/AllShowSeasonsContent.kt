@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.shows.features.seasons.all

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.summary.shows.features.seasons.all.ui.SeasonEpisodesSection
import tv.trakt.trakt.core.summary.shows.features.seasons.all.ui.SeasonInfoSection
import tv.trakt.trakt.core.summary.shows.features.seasons.all.ui.SeasonReviewsSection
import tv.trakt.trakt.core.summary.shows.features.seasons.all.ui.SeasonsModeButtons
import tv.trakt.trakt.core.summary.shows.features.seasons.all.ui.SeasonsTitleBar
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonItem
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonsMode
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonsPeopleMode
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons
import tv.trakt.trakt.core.summary.shows.features.seasons.ui.ShowSeasonsList
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SEASON_ITEM_WIDTH_DP = 92
private const val SEASON_ITEM_SPACING_DP = 8

@Composable
internal fun AllShowSeasonsContent(
    state: AllShowSeasonsState,
    modifier: Modifier = Modifier,
    onModeClick: ((SeasonsMode) -> Unit)? = null,
    onPeopleModeClick: ((SeasonsPeopleMode) -> Unit)? = null,
    onCommentsFilterClick: ((CommentsFilter) -> Unit)? = null,
    onCommentRepliesClick: ((Comment) -> Unit)? = null,
    onCommentReactionsRequest: ((Comment) -> Unit)? = null,
    onCommentReactionClick: ((Reaction, Comment) -> Unit)? = null,
    onNewCommentClick: (() -> Unit)? = null,
    onCommentReplyClick: ((Comment) -> Unit)? = null,
    onCommentReplyUserClick: ((Comment, User) -> Unit)? = null,
    onCommentDeleteClick: ((Comment) -> Unit)? = null,
    onCommentReplyDeleteClick: ((Comment) -> Unit)? = null,
    onPersonClick: ((Person) -> Unit)? = null,
    onSeasonRatingClick: ((Int) -> Unit)? = null,
    onSeasonRatingRemoveClick: (() -> Unit)? = null,
    onEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onSeasonClick: ((SeasonItem) -> Unit)? = null,
    onCheckEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onCheckEpisodeLongClick: ((EpisodeItem) -> Unit)? = null,
    onMoreClick: ((EpisodeItem) -> Unit)? = null,
    onCheckSeasonClick: (() -> Unit)? = null,
    onRemoveSeasonClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
) {
    val peopleSearchState = rememberTextFieldState()
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    val contentPadding = PaddingValues(
        horizontal = TraktTheme.spacing.mainPageHorizontalSpace,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        ScrollableBackdropImage(
            imageUrl = state.backgroundUrl,
            translation = listScrollConnection.resultOffset,
            imageAlpha = 0.1F,
        )

        LazyColumn(
            state = listState,
            overscrollEffect = null,
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding()
                    .plus(TraktTheme.size.navigationBarHeight)
                    .plus(TraktTheme.spacing.mainPageBottomSpace),
            ),
        ) {
            item {
                SeasonsTitleBar(
                    state = state,
                    title = state.items.selectedSeason?.number?.let {
                        when (it) {
                            0 -> stringResource(R.string.text_season_specials)
                            else -> stringResource(R.string.text_season_number, it)
                        }
                    },
                    subtitle = state.show?.title,
                    onSeasonClick = {
                        onSeasonClick?.invoke(it)
                    },
                    onBackClick = {
                        onBackClick?.invoke()
                    },
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(bottom = 8.dp),
                )
            }

            item {
                Crossfade(
                    targetState = state.loading,
                    animationSpec = tween(300),
                    modifier = Modifier
                        .fillMaxWidth(),
                ) { loading ->
                    when (loading) {
                        Idle, Loading -> SeasonsSkeleton(
                            contentPadding = contentPadding,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Done -> ShowSeasonsList(
                            show = state.show,
                            seasons = state.items.seasons,
                            selectedSeason = state.items.selectedSeason?.number,
                            contentPadding = contentPadding,
                            overscrollEffect = null,
                            itemWidth = SEASON_ITEM_WIDTH_DP.dp,
                            itemSpacing = SEASON_ITEM_SPACING_DP.dp,
                            snapScrollEnabled = true,
                            onSeasonClick = onSeasonClick ?: {},
                            onSeasonLongClick = {
                                when {
                                    state.loadingSeason.isLoading -> return@ShowSeasonsList
                                    state.items.isSelectedSeasonWatched -> onRemoveSeasonClick?.invoke()
                                    else -> onCheckSeasonClick?.invoke()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item(
                key = "seasons_mode_buttons",
            ) {
                val padding = 22
                SeasonsModeButtons(
                    mode = state.mode,
                    enabled = !state.loading.isLoading && !state.loading.isIdle,
                    onModeSelect = { onModeClick?.invoke(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                        .padding(top = padding.dp, bottom = (padding - 0.5).dp),
                )
            }

            when (state.mode) {
                SeasonsMode.Episodes -> {
                    SeasonEpisodesSection(
                        state = state,
                        contentPadding = contentPadding,
                        onEpisodeClick = onEpisodeClick,
                        onCheckEpisodeClick = onCheckEpisodeClick,
                        onCheckEpisodeLongClick = onCheckEpisodeLongClick,
                        onMoreClick = onMoreClick,
                    )
                }
                SeasonsMode.Info -> {
                    SeasonInfoSection(
                        state = state,
                        contentPadding = contentPadding,
                        searchState = peopleSearchState,
                        onPersonClick = { onPersonClick?.invoke(it) },
                        onModeClick = { onPeopleModeClick?.invoke(it) },
                        onRatingClick = { onSeasonRatingClick?.invoke(it) },
                        onRatingRemoveClick = { onSeasonRatingRemoveClick?.invoke() },
                    )
                }
                SeasonsMode.Reviews -> {
                    SeasonReviewsSection(
                        state = state,
                        contentPadding = contentPadding,
                        onFilterClick = { onCommentsFilterClick?.invoke(it) },
                        onRepliesClick = { onCommentRepliesClick?.invoke(it) },
                        onRequestReactions = { onCommentReactionsRequest?.invoke(it) },
                        onReactionClick = { reaction, comment ->
                            onCommentReactionClick?.invoke(reaction, comment)
                        },
                        onNewCommentClick = onNewCommentClick,
                        onReplyClick = { onCommentReplyClick?.invoke(it) },
                        onReplyUserClick = { comment, user ->
                            onCommentReplyUserClick?.invoke(comment, user)
                        },
                        onDeleteCommentClick = { onCommentDeleteClick?.invoke(it) },
                        onDeleteReplyClick = { onCommentReplyDeleteClick?.invoke(it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SeasonsSkeleton(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        horizontalArrangement = spacedBy(SEASON_ITEM_SPACING_DP.dp),
        contentPadding = contentPadding,
        userScrollEnabled = false,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        items(count = 5) {
            VerticalMediaSkeletonCard(
                width = SEASON_ITEM_WIDTH_DP.dp,
                secondaryChip = true,
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewLoaded() {
    TraktTheme {
        val seasons = (1..5).map { n ->
            SeasonItem(
                season = PreviewData.season1.copy(
                    ids = PreviewData.season1.ids.copy(trakt = n.toTraktId()),
                    number = n,
                ),
                isWatched = n < 3,
            )
        }.toImmutableList()

        val episodes = (1..6).map { n ->
            EpisodeItem(
                episode = PreviewData.episode1.copy(
                    ids = PreviewData.episode1.ids.copy(trakt = n.toTraktId()),
                    number = n,
                    title = "Episode $n",
                ),
                isWatched = n < 4,
                isCheckable = true,
            )
        }.toImmutableList()

        AllShowSeasonsContent(
            state = AllShowSeasonsState(
                show = PreviewData.show1,
                user = PreviewData.user1,
                loading = Done,
                items = ShowSeasons(
                    seasons = seasons,
                    selectedSeason = PreviewData.season1.copy(number = 1),
                    selectedSeasonEpisodes = episodes,
                ),
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
private fun PreviewLoadedLight() {
    TraktTheme(
        colors = LightColors,
    ) {
        val seasons = (1..5).map { n ->
            SeasonItem(
                season = PreviewData.season1.copy(
                    ids = PreviewData.season1.ids.copy(trakt = n.toTraktId()),
                    number = n,
                ),
                isWatched = n < 3,
            )
        }.toImmutableList()

        val episodes = (1..6).map { n ->
            EpisodeItem(
                episode = PreviewData.episode1.copy(
                    ids = PreviewData.episode1.ids.copy(trakt = n.toTraktId()),
                    number = n,
                    title = "Episode $n",
                ),
                isWatched = n < 4,
                isCheckable = true,
            )
        }.toImmutableList()

        AllShowSeasonsContent(
            state = AllShowSeasonsState(
                show = PreviewData.show1,
                user = PreviewData.user1,
                loading = Done,
                items = ShowSeasons(
                    seasons = seasons,
                    selectedSeason = PreviewData.season1.copy(number = 1),
                    selectedSeasonEpisodes = episodes,
                ),
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
private fun PreviewLoading() {
    TraktTheme {
        AllShowSeasonsContent(
            state = AllShowSeasonsState(
                loading = Loading,
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
private fun PreviewEmpty() {
    TraktTheme {
        AllShowSeasonsContent(
            state = AllShowSeasonsState(
                show = PreviewData.show1,
                loading = Done,
            ),
        )
    }
}
