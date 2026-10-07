package tv.trakt.trakt.core.summary.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarBottomOffset
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.ratings.ui.RatingDelight
import tv.trakt.trakt.core.ratings.ui.RatingDelightOverlay
import tv.trakt.trakt.core.ratings.ui.UserRatingBar
import tv.trakt.trakt.core.ratings.ui.ratingDelight
import tv.trakt.trakt.core.reactions.media.MediaReactionsView
import tv.trakt.trakt.core.reactions.media.MediaReactionsViewModel
import tv.trakt.trakt.core.reactions.media.ui.MediaReactionsUserPick
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

private val PillScrollThreshold = 64.dp

private val PillEntranceDelay = 300.milliseconds
private val CollapseDelay = 600.milliseconds
private val CollapseDelightDelay = 2100.milliseconds

private val PillShape = RoundedCornerShape(18.dp)

private val PillIconSize = 23.dp
private val PillStarSize = 22.dp
private val PillStarLift = 6.dp
private val PillHorizontalPadding = 12.dp
private val PillVerticalPadding = 12.dp
private val PillHeight = PillIconSize + PillVerticalPadding * 2
private val PillTouchPadding = 6.dp
private val PillDragLabelSpacing = 40.dp
private val SeparatorHeight = 16.dp

private val PillReactionFontSize = 20.sp
private val PillReactionCellSize = 28.dp
private val PillLoadingSize = 16.dp

private val PillReactionSlotWidth = PillReactionCellSize + PillTouchPadding * 2

private val PillOuterSpacing = 8.dp

private val PillFootprint = PillHeight + PillOuterSpacing

// The bar anchors to the reaction slot, which sits inset from the pill's top edge.
private val PillDropdownSpacing =
    PillOuterSpacing + (PillHeight - PillReactionSlotWidth) / 2
private val SeparatorInnerPadding = 4.dp

private data class RatingCommit(
    val id: Int,
    val rating: Int?,
)

@Composable
internal fun rememberRatingPillScrolledAway(listState: LazyListState): State<Boolean> {
    val thresholdPx = with(LocalDensity.current) { PillScrollThreshold.toPx() }
    return remember(listState, thresholdPx) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > thresholdPx
        }
    }
}

@Composable
internal fun BoxScope.DetailsRatingPillOverlay(
    visible: Boolean,
    rating: UserRating?,
    favoriteLoading: Boolean,
    reactionsTarget: MediaReactionsTarget?,
    onRatingDrag: (Boolean) -> Unit,
    onRatingClick: (Int) -> Unit,
    onRatingRemoveClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val snackbarOffset = LocalSnackbarBottomOffset.current

    // Held back past the screen's enter transition so the pill's own fade in stays visible.
    var entranceReady by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(PillEntranceDelay)
        entranceReady = true
    }
    val shown = visible && entranceReady

    LaunchedEffect(shown) {
        if (!shown) expanded = false
    }

    // Resume-scoped so stacked details screens hand the shared offset over in order.
    LifecycleResumeEffect(shown) {
        snackbarOffset.value = if (shown) PillFootprint else 0.dp
        onPauseOrDispose { snackbarOffset.value = 0.dp }
    }

    if (expanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onClick(throttle = false) { expanded = false },
        )
    }

    AnimatedVisibility(
        visible = shown,
        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 10 },
        exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 10 },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(
                end = TraktTheme.spacing.mainPageHorizontalSpace,
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding()
                    .plus(TraktTheme.size.navigationBarHeight)
                    .plus(PillOuterSpacing),
            ),
    ) {
        val reactionSlot: (@Composable () -> Unit)? = reactionsTarget?.let { target ->
            { PillReactionSlot(target) }
        }
        DetailsRatingPill(
            rating = rating,
            favoriteLoading = favoriteLoading,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onRatingDrag = onRatingDrag,
            onRatingClick = onRatingClick,
            onRatingRemoveClick = onRatingRemoveClick,
            onFavoriteClick = onFavoriteClick,
            reaction = reactionSlot,
        )
    }
}

@Composable
private fun PillReactionSlot(target: MediaReactionsTarget) {
    val viewModel: MediaReactionsViewModel = koinViewModel(
        parameters = { parametersOf(target) },
    )
    val reactionsState by viewModel.state.collectAsStateWithLifecycle()

    Box(contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = !reactionsState.loading.isDone,
            enter = EnterTransition.None,
            exit = fadeOut(tween(200, delayMillis = 350)),
        ) {
            PillReactionLoading()
        }

        MediaReactionsView(
            viewModel = viewModel,
            dropdownSpacing = PillDropdownSpacing,
            anchor = { state, anchorModifier ->
                PillReaction(
                    userReactions = state.userReactions,
                    modifier = anchorModifier,
                )
            },
        )
    }
}

@Composable
internal fun DetailsRatingPill(
    rating: UserRating?,
    favoriteLoading: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onRatingDrag: (Boolean) -> Unit = {},
    onRatingClick: (Int) -> Unit = {},
    onRatingRemoveClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    reaction: (@Composable () -> Unit)? = null,
) {
    var commit by remember { mutableStateOf<RatingCommit?>(null) }
    var ratingDragging by remember { mutableStateOf(false) }

    LaunchedEffect(commit) {
        val current = commit ?: return@LaunchedEffect
        val hasDelight = current.rating?.let(::ratingDelight) != null
        delay(if (hasDelight) CollapseDelightDelay else CollapseDelay)
        onExpandedChange(false)
        commit = null
    }

    val dragAlpha by animateFloatAsState(
        targetValue = if (ratingDragging) 0.05F else 1F,
        animationSpec = tween(150),
        label = "dragAlpha",
    )

    BackHandler(enabled = expanded) {
        onExpandedChange(false)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .dropShadow(
                shape = PillShape,
                shadow = Shadow(
                    radius = 6.dp,
                    color = Color.Black,
                    spread = 1.dp,
                    alpha = 0.12F,
                ),
            )
            .background(TraktTheme.colors.navigationContainer, PillShape)
            .height(PillHeight)
            .padding(horizontal = PillHorizontalPadding - PillTouchPadding),
    ) {
        AnimatedContent(
            targetState = expanded,
            contentAlignment = Alignment.CenterEnd,
            transitionSpec = {
                fadeIn(tween(150, delayMillis = 75)) togetherWith
                    fadeOut(tween(75)) using
                    SizeTransform(clip = false) { _, _ -> tween(200) }
            },
            label = "ratingPill",
        ) { isExpanded ->
            when {
                isExpanded -> UserRatingBar(
                    rating = rating?.rating,
                    favoriteVisible = false,
                    size = PillStarSize,
                    activeLift = PillStarLift,
                    textSpacing = PillDragLabelSpacing,
                    topPadding = 0.dp,
                    onRatingDrag = {
                        ratingDragging = it
                        onRatingDrag(it)
                    },
                    onRatingClick = {
                        onRatingClick(it)
                        commit = RatingCommit(id = (commit?.id ?: 0) + 1, rating = it)
                    },
                    onRatingRemoveClick = {
                        onRatingRemoveClick()
                        commit = RatingCommit(id = (commit?.id ?: 0) + 1, rating = null)
                    },
                    modifier = Modifier.padding(horizontal = PillTouchPadding),
                )

                else -> CollapsedRating(
                    rating = rating,
                    onClick = { onExpandedChange(true) },
                )
            }
        }

        PillSeparator()

        PillFavorite(
            favorite = rating?.favorite == true,
            loading = favoriteLoading,
            dimmed = ratingDragging,
            onClick = onFavoriteClick,
        )

        if (reaction != null) {
            PillSeparator()
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(PillReactionSlotWidth)
                    .alpha(dragAlpha),
            ) {
                reaction()
            }
        }
    }
}

@Composable
private fun PillReaction(
    userReactions: ImmutableList<MediaReaction>,
    modifier: Modifier = Modifier,
) {
    MediaReactionsUserPick(
        userReactions = userReactions,
        emojiSize = PillReactionCellSize,
        emojiFontSize = PillReactionFontSize,
        addIconSize = PillIconSize,
        modifier = modifier.padding(PillTouchPadding),
    )
}

@Composable
private fun PillReactionLoading(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(PillIconSize),
    ) {
        CircularProgressIndicator(
            color = TraktTheme.colors.separator,
            trackColor = Color.Transparent,
            strokeWidth = 1.5.dp,
            modifier = Modifier.size(PillLoadingSize),
        )
    }
}

@Composable
private fun PillSeparator() {
    Box(
        modifier = Modifier
            .padding(horizontal = SeparatorInnerPadding)
            .size(width = 1.dp, height = SeparatorHeight)
            .background(TraktTheme.colors.separator),
    )
}

@Composable
private fun CollapsedRating(
    rating: UserRating?,
    onClick: () -> Unit,
) {
    val rated = (rating?.rating ?: 0) > 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = Modifier
            .onClick(onClick = onClick)
            .padding(PillTouchPadding),
    ) {
        Icon(
            painter = painterResource(
                if (rated) R.drawable.ic_star_trakt_on else R.drawable.ic_star_trakt_off,
            ),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(PillStarSize),
        )
        if (rated && rating != null) {
            Text(
                text = rating.rating5Scale,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.meta.copy(fontSize = 15.sp),
            )
        }
    }
}

@Composable
private fun PillFavorite(
    favorite: Boolean,
    loading: Boolean,
    dimmed: Boolean,
    onClick: () -> Unit,
) {
    var favoriteDelightId by remember { mutableIntStateOf(0) }
    var favoriteDelightPending by remember { mutableStateOf(false) }
    LaunchedEffect(favorite, favoriteDelightPending) {
        if (!favorite || !favoriteDelightPending) return@LaunchedEffect
        favoriteDelightPending = false
        favoriteDelightId++
    }

    val favoriteAlpha by animateFloatAsState(
        targetValue = when {
            dimmed -> 0.05F
            loading -> 0.5F
            else -> 1F
        },
        animationSpec = tween(150),
        label = "favoriteAlpha",
    )

    Box(
        modifier = Modifier
            .onClick(enabled = !loading) {
                favoriteDelightPending = !favorite
                onClick()
            }
            .padding(PillTouchPadding),
    ) {
        Icon(
            painter = painterResource(
                if (favorite) R.drawable.ic_heart_on else R.drawable.ic_heart_off,
            ),
            contentDescription = null,
            tint = if (favorite) Red500 else TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(PillIconSize)
                .alpha(favoriteAlpha),
        )

        if (favoriteDelightId > 0) {
            val center = with(LocalDensity.current) { PillIconSize.toPx() / 2 }
            RatingDelightOverlay(
                delight = RatingDelight.FavoriteGlow,
                origin = Offset(center, center),
                key = favoriteDelightId,
                modifier = Modifier.size(PillIconSize),
            )
        }
    }
}

@Preview
@Composable
private fun DetailsRatingPillPreview() {
    TraktThemeLightDark {
        Column(
            verticalArrangement = spacedBy(16.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(16.dp),
        ) {
            DetailsRatingPill(
                rating = null,
                favoriteLoading = false,
                expanded = false,
                onExpandedChange = {},
                reaction = {
                    PillReactionLoading()
                },
            )
            DetailsRatingPill(
                rating = UserRating(
                    mediaId = TraktId(1),
                    mediaType = MediaType.Movie,
                    rating = 7,
                    favorite = true,
                ),
                favoriteLoading = false,
                expanded = false,
                onExpandedChange = {},
                reaction = {
                    PillReaction(userReactions = persistentListOf())
                },
            )
            DetailsRatingPill(
                rating = UserRating(
                    mediaId = TraktId(1),
                    mediaType = MediaType.Movie,
                    rating = 8,
                ),
                favoriteLoading = false,
                expanded = true,
                onExpandedChange = {},
                reaction = {
                    PillReaction(userReactions = persistentListOf(MediaReaction.Fire))
                },
            )
        }
    }
}
