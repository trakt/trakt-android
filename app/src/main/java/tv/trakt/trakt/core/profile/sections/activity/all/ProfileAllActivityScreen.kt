@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.profile.sections.activity.all

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.features.details.CommentDetailsSheet
import tv.trakt.trakt.core.profile.sections.activity.all.ui.ProfileAllActivityEpisodeItem
import tv.trakt.trakt.core.profile.sections.activity.all.ui.ProfileAllActivityMovieItem
import tv.trakt.trakt.core.profile.sections.activity.all.ui.ProfileAllActivityShowItem
import tv.trakt.trakt.core.profile.sections.activity.all.ui.filters.ProfileActivityFilters
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter.Comments
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter.Ratings
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileCommentItem
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileRatingItem
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileRatingItem.EpisodeItem
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileRatingItem.MovieItem
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileRatingItem.ShowItem
import tv.trakt.trakt.core.profile.sections.activity.ui.comments.ProfileCommentItemView
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ProfileAllActivityScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileAllActivityViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var commentSheet by remember { mutableStateOf<Comment?>(null) }

    LaunchedEffect(
        state.navigateShow,
        state.navigateMovie,
        state.navigateEpisode,
    ) {
        state.navigateShow?.let { onShowClick(it) }
        state.navigateEpisode?.let { onEpisodeClick(it.first, it.second) }
        state.navigateMovie?.let { onMovieClick(it) }
        viewModel.clearNavigation()
    }

    ProfileAllActivityContent(
        state = state,
        modifier = modifier,
        onLoadMore = { viewModel.loadMoreData() },
        onRatingClick = { item ->
            when (item) {
                is ShowItem -> viewModel.navigateToShow(item.show)
                is MovieItem -> viewModel.navigateToMovie(item.movie)
                is EpisodeItem -> viewModel.navigateToEpisode(item.show, item.episode)
            }
        },
        onRatingShowClick = viewModel::navigateToShow,
        onCommentClick = { commentSheet = it.comment },
        onCommentShowClick = viewModel::navigateToShow,
        onCommentMovieClick = viewModel::navigateToMovie,
        onCommentEpisodeClick = viewModel::navigateToEpisode,
        onRequestReactions = { viewModel.loadReactions(it.id) },
        onFilterClick = { viewModel.setFilter(it) },
        onBackClick = onNavigateBack,
    )

    CommentDetailsSheet(
        comment = commentSheet,
        onDeleteComment = {
            viewModel.loadData(ignoreErrors = true)
        },
        onDismiss = {
            commentSheet = null
        },
    )
}

@Composable
internal fun ProfileAllActivityContent(
    state: ProfileAllActivityState,
    modifier: Modifier = Modifier,
    onLoadMore: () -> Unit = {},
    onRatingClick: (ProfileRatingItem) -> Unit = {},
    onRatingShowClick: (Show) -> Unit = {},
    onCommentClick: (ProfileCommentItem) -> Unit = {},
    onCommentShowClick: (Show) -> Unit = {},
    onCommentMovieClick: (Movie) -> Unit = {},
    onCommentEpisodeClick: (Show, Episode) -> Unit = { _, _ -> },
    onRequestReactions: (Comment) -> Unit = {},
    onFilterClick: (ProfileActivityFilter) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        val contentPadding = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight * 2),
        )

        ScrollableBackdropImage(
            translation = listScrollConnection.resultOffset,
        )

        val filter = state.filter ?: Ratings

        ContentList(
            listState = listState,
            listFilter = filter,
            ratingItems = (state.ratingItems ?: emptyList()).toImmutableList(),
            commentItems = (state.commentItems ?: emptyList()).toImmutableList(),
            reactions = state.reactions ?: persistentMapOf(),
            contentPadding = contentPadding,
            loading = state.loading.isLoading,
            loadingMore = state.loadingMore.isLoading,
            onEndOfList = onLoadMore,
            onRatingClick = onRatingClick,
            onRatingShowClick = onRatingShowClick,
            onCommentClick = onCommentClick,
            onCommentShowClick = onCommentShowClick,
            onCommentMovieClick = onCommentMovieClick,
            onCommentEpisodeClick = onCommentEpisodeClick,
            onRequestReactions = onRequestReactions,
            onFilterClick = onFilterClick,
            onBackClick = onBackClick,
        )

        if (state.error != null) {
            Text(
                text = "${
                    stringResource(
                        R.string.error_text_unexpected_error_short,
                    )
                }\n\n${state.error}",
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta,
                maxLines = 10,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(contentPadding),
            )
        }
    }
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .height(TraktTheme.size.titleBarHeight)
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
            title = stringResource(R.string.list_title_activity),
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    listFilter: ProfileActivityFilter,
    ratingItems: ImmutableList<ProfileRatingItem>,
    commentItems: ImmutableList<ProfileCommentItem>,
    reactions: ImmutableMap<Int, ReactionsSummary>,
    contentPadding: PaddingValues,
    loading: Boolean,
    loadingMore: Boolean,
    onEndOfList: () -> Unit,
    onRatingClick: (ProfileRatingItem) -> Unit,
    onRatingShowClick: (Show) -> Unit,
    onCommentClick: (ProfileCommentItem) -> Unit,
    onCommentShowClick: (Show) -> Unit,
    onCommentMovieClick: (Movie) -> Unit,
    onCommentEpisodeClick: (Show, Episode) -> Unit,
    onRequestReactions: (Comment) -> Unit,
    onFilterClick: (ProfileActivityFilter) -> Unit,
    onBackClick: () -> Unit,
) {
    val currentSize = when (listFilter) {
        Ratings -> ratingItems.size
        Comments -> commentItems.size
    }

    val isEmpty = currentSize == 0

    val isScrolledToBottom by remember(currentSize) {
        derivedStateOf {
            listState.firstVisibleItemIndex >= (currentSize - 5)
        }
    }

    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            onEndOfList()
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        item {
            TitleBar(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .onClick { onBackClick() },
            )
        }

        item {
            ProfileActivityFilters(
                selected = listFilter,
                onClick = onFilterClick,
                height = 32.dp,
                unselectedTextVisible = true,
                paddingVertical = PaddingValues(bottom = 19.dp),
            )
        }

        when (listFilter) {
            Ratings -> {
                items(
                    items = ratingItems,
                    key = { it.key },
                ) { item ->
                    val itemModifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        )

                    when (item) {
                        is ShowItem -> ProfileAllActivityShowItem(
                            item = item,
                            onClick = { onRatingClick(item) },
                            modifier = itemModifier,
                        )
                        is MovieItem -> ProfileAllActivityMovieItem(
                            item = item,
                            onClick = { onRatingClick(item) },
                            modifier = itemModifier,
                        )
                        is EpisodeItem -> ProfileAllActivityEpisodeItem(
                            item = item,
                            onClick = { onRatingClick(item) },
                            onShowClick = { onRatingShowClick(item.show) },
                            modifier = itemModifier,
                        )
                    }
                }
            }
            Comments -> {
                items(
                    items = commentItems,
                    key = { it.comment.id },
                ) { item ->
                    ProfileCommentItemView(
                        item = item,
                        reactions = reactions,
                        onClick = { onCommentClick(item) },
                        onRepliesClick = { onCommentClick(item) },
                        onShowClick = onCommentShowClick,
                        onMovieClick = onCommentMovieClick,
                        onEpisodeClick = onCommentEpisodeClick,
                        onRequestReactions = onRequestReactions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }
            }
        }

        if (loading && isEmpty) {
            items(10) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        } else if (loadingMore && !isEmpty) {
            items(1) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        } else if (!loading && isEmpty) {
            item {
                Text(
                    text = stringResource(R.string.list_placeholder_empty),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(
                            fadeInSpec = tween(200),
                            fadeOutSpec = tween(200),
                        ),
                )
            }
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        ProfileAllActivityContent(
            state = ProfileAllActivityState(),
        )
    }
}
