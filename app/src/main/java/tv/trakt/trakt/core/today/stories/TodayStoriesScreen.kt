package tv.trakt.trakt.core.today.stories

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import coil3.compose.AsyncImage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.today.model.TodayStoryFrame
import tv.trakt.trakt.core.today.model.TodayStoryGroup
import tv.trakt.trakt.core.today.stories.ui.TodayStoryFrameContent
import tv.trakt.trakt.helpers.extensions.rememberAnimationsDisabled
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private const val FRAME_DURATION_MS = 10_000
private const val BACKDROP_ALPHA = 0.35F
private const val MIN_PAGE_ALPHA = 0.05F

private val BackdropBlur = 24.dp
private val SegmentHeight = 3.dp
private val SegmentSpace = 4.dp
private val HeaderIconSize = 23.dp
private val HeaderButtonSize = 40.dp

// Touch box padding plus the empty space inside each glyph, so the icons line up
// with the ends of the progress bar.
private val BackArrowInset = (HeaderButtonSize - HeaderIconSize) / 2 + 3.dp
private val InfoInset = (HeaderButtonSize - HeaderIconSize) / 2 + 1.dp
private val SwipeThreshold = 56.dp
private val SnapVelocityThreshold = 400.dp

private val PosterTopGap = 24.dp

@Composable
internal fun TodayStoriesScreen(
    viewModel: TodayStoriesViewModel,
    onNavigateToUser: (User) -> Unit,
    onNavigateToSocial: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val bottomBarVisibility = LocalBottomBarVisibility.current
    DisposableEffect(Unit) {
        bottomBarVisibility.value = false
        onDispose {
            bottomBarVisibility.value = true
        }
    }

    val navigateBack by rememberUpdatedState(onNavigateBack)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                TodayStoriesEvent.Close -> navigateBack()
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    TodayStoriesContent(
        state = state,
        onNext = viewModel::next,
        onPrevious = viewModel::previous,
        onSelectGroup = viewModel::selectGroup,
        onNextGroup = viewModel::nextGroup,
        onPreviousGroup = viewModel::previousGroup,
        onUserClick = { user ->
            viewModel.leave()
            onNavigateToUser(user)
        },
        onSocialClick = {
            viewModel.leave()
            onNavigateToSocial()
        },
        onRevealSpoiler = viewModel::logSpoilerReveal,
        onClose = onNavigateBack,
    )
}

@Composable
private fun TodayStoriesContent(
    state: TodayStoriesState,
    modifier: Modifier = Modifier,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onSelectGroup: (Int) -> Unit = {},
    onNextGroup: () -> Unit = {},
    onPreviousGroup: () -> Unit = {},
    onUserClick: (User) -> Unit = {},
    onSocialClick: () -> Unit = {},
    onRevealSpoiler: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val background = TraktTheme.colors.backgroundPrimary
    val horizontalPadding = TraktTheme.spacing.mainPageHorizontalSpace

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
    ) {
        val group = state.group ?: return@Box
        val frame = state.frame ?: return@Box

        var isFlipped by remember(frame.key) { mutableStateOf(false) }
        var isHeld by remember(group.key) { mutableStateOf(false) }
        var isConfirming by remember(group.key) { mutableStateOf(false) }

        val pagerState = rememberPagerState(initialPage = state.groupIndex) { state.groups.size }

        val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
        val isPaused = isHeld ||
            isFlipped ||
            isConfirming ||
            pagerState.isScrollInProgress ||
            !lifecycle.isAtLeast(Lifecycle.State.RESUMED)

        TodayStoryBackdrop(
            state = state,
            pagerState = pagerState,
        )

        // Only the story moves between groups. The progress bar and header stay put,
        // since sliding the whole screen causes motion sickness.
        Column(
            verticalArrangement = spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0F to background.copy(alpha = 0.4F),
                        0.3F to background.copy(alpha = 0.1F),
                        0.7F to background,
                    ),
                )
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                ),
        ) {
            TodayStoryProgress(
                frameKeys = group.frames.map { it.key },
                currentIndex = state.frameIndex,
                isPaused = isPaused,
                onFinished = onNext,
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )

            TodayStoryHeader(
                title = {
                    TodayStoryPagerTitle(
                        state = state,
                        pagerState = pagerState,
                    )
                },
                onDetailsClick = { isFlipped = !isFlipped },
                onSocialClick = onSocialClick,
                onClose = onClose,
                modifier = Modifier.padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = PosterTopGap - 16.dp,
                ),
            )

            // Spans the full width so the sliding story is not cut off at the page padding.
            TodayStoryPager(
                state = state,
                pagerState = pagerState,
                onSelectGroup = onSelectGroup,
                onNext = onNext,
                onPrevious = onPrevious,
                onNextGroup = onNextGroup,
                onPreviousGroup = onPreviousGroup,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F),
            ) { storyFrame, isActive ->
                TodayStoryFrameContent(
                    frame = storyFrame,
                    isFlipped = isActive && isFlipped,
                    onFlip = { if (isActive) isFlipped = !isFlipped },
                    onNext = { if (isActive) onNext() },
                    onPrevious = { if (isActive) onPrevious() },
                    onHeldChange = { if (isActive) isHeld = it },
                    onUserClick = onUserClick,
                    onConfirmVisibleChange = { if (isActive) isConfirming = it },
                    onRevealSpoiler = onRevealSpoiler,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding),
                )
            }
        }
    }
}

/** Blends the titles of the two pages around the swipe position. */
@Composable
private fun TodayStoryPagerTitle(
    state: TodayStoriesState,
    pagerState: PagerState,
) {
    val startPage by rememberStartPage(pagerState)

    Box {
        listOf(startPage, startPage + 1).forEach { page ->
            val group = state.groups.getOrNull(page) ?: return@forEach

            key(page) {
                TodayStoryTitle(
                    title = when (group) {
                        is TodayStoryGroup.Title -> group.media.title
                        is TodayStoryGroup.ForYou -> stringResource(R.string.text_today_for_you)
                    },
                    position = stringResource(
                        R.string.text_today_story_position,
                        page + 1,
                        state.groups.size,
                    ),
                    modifier = Modifier.graphicsLayer {
                        alpha = pagerState.pageVisibility(page)
                    },
                )
            }
        }
    }
}

@Composable
private fun TodayStoryTitle(
    title: String,
    position: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = TraktTheme.typography.cardTitle.copy(
                fontSize = 14.sp,
            ),
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = position,
            style = TraktTheme.typography.cardSubtitle,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
        )
    }
}

/** Blends the backdrops of the two pages around the swipe position, so it fades as the story slides. */
@Composable
private fun TodayStoryBackdrop(
    state: TodayStoriesState,
    pagerState: PagerState,
) {
    val startPage by rememberStartPage(pagerState)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(BACKDROP_ALPHA)
            .blur(BackdropBlur),
    ) {
        listOf(startPage, startPage + 1).forEach { page ->
            val frame = state.frameForGroup(page) ?: return@forEach

            key(page) {
                AsyncImage(
                    model = frame.media.images?.getPosterUrl(Images.Size.MEDIUM),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (page == startPage) return@graphicsLayer
                            alpha = pagerState.pageVisibility(page)
                        },
                )
            }
        }
    }
}

@Composable
private fun TodayStoryPager(
    state: TodayStoriesState,
    pagerState: PagerState,
    onSelectGroup: (Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onNextGroup: () -> Unit,
    onPreviousGroup: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (frame: TodayStoryFrame, isActive: Boolean) -> Unit,
) {
    val animationsDisabled = rememberAnimationsDisabled()
    val selectGroup by rememberUpdatedState(onSelectGroup)
    var isUserSwipe by remember { mutableStateOf(false) }

    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    var isSkipPressed by remember { mutableStateOf(false) }
    val isSettling = pagerState.isScrollInProgress && !isDragged

    LaunchedEffect(state.groupIndex) {
        if (pagerState.currentPage == state.groupIndex) return@LaunchedEffect

        isUserSwipe = false
        if (animationsDisabled) {
            pagerState.scrollToPage(state.groupIndex)
        } else {
            pagerState.animateScrollToPage(state.groupIndex)
        }
    }

    // Only swipes report the page back, so a programmatic scroll cancelled midway cannot select
    // the page it stopped on. A swipe that interrupts one still reports, even when it returns to
    // the page it started from.
    LaunchedEffect(pagerState) {
        launch {
            pagerState.interactionSource.interactions
                .filterIsInstance<DragInteraction.Start>()
                .collect { isUserSwipe = true }
        }
        snapshotFlow { pagerState.isScrollInProgress }
            .filter { !it }
            .collect {
                if (!isUserSwipe) return@collect
                isUserSwipe = false
                selectGroup(pagerState.currentPage)
            }
    }

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            key = { state.groups[it].key },
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(
                    rememberEdgeSwipeConnection(
                        onSwipePastStart = onPreviousGroup,
                        onSwipePastEnd = onNextGroup,
                    ),
                ),
        ) { page ->
            val frame = state.frameForGroup(page) ?: return@HorizontalPager

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = lerp(MIN_PAGE_ALPHA, 1F, pagerState.pageVisibility(page))
                    },
            ) {
                content(frame, page == state.groupIndex)
            }
        }

        // A touch on a settling pager would grab it and hold the slide. This layer takes those
        // touches instead, so stories can be tapped or dragged through in quick succession.
        if (isSettling || isSkipPressed) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .skipGestures(
                        pagerState = pagerState,
                        onPressedChange = { isSkipPressed = it },
                        onNext = onNext,
                        onPrevious = onPrevious,
                        onSelectGroup = onSelectGroup,
                    ),
            )
        }
    }
}

/** Taps step by half of the screen, drags carry on from wherever the slide is. */
@Composable
private fun Modifier.skipGestures(
    pagerState: PagerState,
    onPressedChange: (Boolean) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSelectGroup: (Int) -> Unit,
): Modifier {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val animationsDisabled = rememberAnimationsDisabled()
    val scope = rememberCoroutineScope()
    val pressedChange by rememberUpdatedState(onPressedChange)
    val next by rememberUpdatedState(onNext)
    val previous by rememberUpdatedState(onPrevious)
    val selectGroup by rememberUpdatedState(onSelectGroup)

    return pointerInput(isRtl, pagerState) {
        val velocityThreshold = SnapVelocityThreshold.toPx()

        awaitEachGesture {
            val down = awaitFirstDown()
            down.consume()
            pressedChange(true)

            val velocityTracker = VelocityTracker()
            velocityTracker.addPosition(down.uptimeMillis, down.position)

            var drag: Channel<Float>? = null
            var releaseVelocity = 0F
            var distance = 0F
            var isReleased = false

            try {
                while (true) {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                    val delta = change.positionChange().x
                    change.consume()

                    if (!change.pressed) {
                        isReleased = true
                        break
                    }

                    velocityTracker.addPointerInputChange(change)
                    distance += delta

                    if (drag == null && abs(distance) > viewConfiguration.touchSlop) {
                        val deltas = Channel<Float>(Channel.UNLIMITED)
                        drag = deltas
                        scope.launch {
                            pagerState.scroll(MutatePriority.UserInput) {
                                for (scrollDelta in deltas) scrollBy(scrollDelta)
                            }

                            val target = pagerState.settleTarget(
                                forwardVelocity = releaseVelocity,
                                velocityThreshold = velocityThreshold,
                            )
                            selectGroup(target)
                            if (animationsDisabled) {
                                pagerState.scrollToPage(target)
                            } else {
                                pagerState.animateScrollToPage(target)
                            }
                        }
                    }

                    drag?.trySend(if (isRtl) delta else -delta)
                }

                if (drag != null) {
                    val velocity = velocityTracker.calculateVelocity().x
                    releaseVelocity = if (isRtl) velocity else -velocity
                    return@awaitEachGesture
                }
                if (!isReleased) return@awaitEachGesture

                val isStart = (down.position.x < size.width / 2) != isRtl
                if (isStart) previous() else next()
            } finally {
                drag?.close()
                pressedChange(false)
            }
        }
    }
}

/** Picks the page a released drag lands on, flinging one page past the swipe position when fast. */
private fun PagerState.settleTarget(
    forwardVelocity: Float,
    velocityThreshold: Float,
): Int {
    val position = currentPage + currentPageOffsetFraction
    val target = when {
        forwardVelocity > velocityThreshold -> floor(position).toInt() + 1
        forwardVelocity < -velocityThreshold -> ceil(position).toInt() - 1
        else -> position.roundToInt()
    }
    return target.coerceIn(0, pageCount - 1)
}

/** The page at or just before the swipe position; the next page is the one fading in. */
@Composable
private fun rememberStartPage(pagerState: PagerState): State<Int> {
    return remember(pagerState) {
        derivedStateOf {
            floor(pagerState.currentPage + pagerState.currentPageOffsetFraction).toInt()
        }
    }
}

/** 1 when the page is centred, falling to 0 once it is a full page away. */
private fun PagerState.pageVisibility(page: Int): Float {
    return 1F - abs(getOffsetDistanceInPages(page)).coerceAtMost(1F)
}

/** Swiping past the first or last story closes the viewer. */
@Composable
private fun rememberEdgeSwipeConnection(
    onSwipePastStart: () -> Unit,
    onSwipePastEnd: () -> Unit,
): NestedScrollConnection {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val threshold = with(LocalDensity.current) { SwipeThreshold.toPx() }
    val swipePastStart by rememberUpdatedState(onSwipePastStart)
    val swipePastEnd by rememberUpdatedState(onSwipePastEnd)

    return remember(isRtl, threshold) {
        object : NestedScrollConnection {
            private var overscroll = 0F

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source == NestedScrollSource.UserInput) {
                    overscroll += available.x
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val total = overscroll
                overscroll = 0F
                if (abs(total) < threshold) return Velocity.Zero

                val towardsStart = (total > 0) != isRtl
                if (towardsStart) swipePastStart() else swipePastEnd()
                return Velocity.Zero
            }
        }
    }
}

@Composable
private fun TodayStoryProgress(
    frameKeys: List<String>,
    currentIndex: Int,
    isPaused: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val finished by rememberUpdatedState(onFinished)
    val currentKey = frameKeys.getOrNull(currentIndex)
    val progress = remember(currentKey) { Animatable(0F) }
    val animationsDisabled = rememberAnimationsDisabled()

    LaunchedEffect(currentKey, isPaused) {
        if (isPaused) return@LaunchedEffect

        val remaining = ((1F - progress.value) * FRAME_DURATION_MS).toLong()

        if (animationsDisabled) {
            val startedAt = SystemClock.uptimeMillis()
            val startProgress = progress.value
            try {
                delay(remaining.milliseconds)
            } finally {
                val elapsed = SystemClock.uptimeMillis() - startedAt
                progress.snapTo(
                    (startProgress + elapsed.toFloat() / FRAME_DURATION_MS).coerceAtMost(1F),
                )
            }
        } else {
            progress.animateTo(
                targetValue = 1F,
                animationSpec = tween(durationMillis = remaining.toInt(), easing = LinearEasing),
            )
        }
        finished()
    }

    val shape = RoundedCornerShape(2.dp)
    val filled = TraktTheme.colors.textPrimary
    val empty = filled.copy(alpha = 0.25F)

    Row(
        horizontalArrangement = spacedBy(SegmentSpace),
        modifier = modifier.fillMaxWidth(),
    ) {
        frameKeys.forEachIndexed { index, _ ->
            val fill = when {
                index < currentIndex -> 1F
                index == currentIndex -> progress.value
                else -> 0F
            }

            Box(
                modifier = Modifier
                    .weight(1F)
                    .height(SegmentHeight)
                    .clip(shape)
                    .background(empty),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fill)
                        .background(filled),
                )
            }
        }
    }
}

@Composable
private fun TodayStoryHeader(
    title: @Composable () -> Unit,
    onDetailsClick: () -> Unit,
    onSocialClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        TodayStoryHeaderButton(
            iconRes = R.drawable.ic_back_arrow,
            onClick = onClose,
            modifier = Modifier.pullTowardsEdge(start = BackArrowInset),
        )

        Box(
            modifier = Modifier
                .weight(1F)
                .padding(end = 8.dp),
        ) {
            Box(
                modifier = Modifier.onClick(onClick = onClose),
            ) {
                title()
            }
        }

        TodayStoryHeaderButton(
            iconRes = R.drawable.ic_info,
            onClick = onDetailsClick,
        )

        TodayStoryHeaderButton(
            iconRes = R.drawable.ic_grid,
            onClick = onSocialClick,
            modifier = Modifier.pullTowardsEdge(end = InfoInset),
        )
    }
}

@Composable
private fun TodayStoryHeaderButton(
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(HeaderButtonSize)
            .onClick(onClick = onClick),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(HeaderIconSize),
        )
    }
}

/** Lets content overhang its start or end edge while keeping its full touch area. */
private fun Modifier.pullTowardsEdge(
    start: Dp = 0.dp,
    end: Dp = 0.dp,
): Modifier {
    return layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val startPx = start.roundToPx()
        val endPx = end.roundToPx()

        layout((placeable.width - startPx - endPx).coerceAtLeast(0), placeable.height) {
            placeable.placeRelative(-startPx, 0)
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun PreviewHeader() {
    TraktTheme {
        TodayStoryHeader(
            title = {
                TodayStoryTitle(
                    title = "Severance",
                    position = "1 / 12",
                )
            },
            onDetailsClick = {},
            onSocialClick = {},
            onClose = {},
            modifier = Modifier
                .background(Color.Black)
                .padding(horizontal = 16.dp),
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun PreviewHeaderLongTitle() {
    TraktTheme {
        TodayStoryHeader(
            title = {
                TodayStoryTitle(
                    title = "The Lord of the Rings: The Rings of Power - Extended Edition Special",
                    position = "3 / 12",
                )
            },
            onDetailsClick = {},
            onSocialClick = {},
            onClose = {},
            modifier = Modifier
                .background(Color.Black)
                .padding(horizontal = 16.dp),
        )
    }
}
