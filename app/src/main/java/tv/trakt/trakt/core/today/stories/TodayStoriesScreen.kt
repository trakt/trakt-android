package tv.trakt.trakt.core.today.stories

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.core.today.model.TodayMedia
import tv.trakt.trakt.core.today.model.TodayStoryGroup
import tv.trakt.trakt.core.today.model.TodayTitleStory
import tv.trakt.trakt.core.today.stories.ui.TodayStoryFrameContent
import tv.trakt.trakt.helpers.extensions.rememberAnimationsDisabled
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant
import kotlin.math.abs

private const val FRAME_DURATION_MS = 10_000
private const val BACKDROP_ALPHA = 0.35F
private const val STORY_TRANSITION_MS = 150

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

private val PosterTopGap = 24.dp

@Composable
internal fun TodayStoriesScreen(
    viewModel: TodayStoriesViewModel,
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
        onNextGroup = viewModel::nextGroup,
        onPreviousGroup = viewModel::previousGroup,
        onClose = onNavigateBack,
    )
}

@Composable
private fun TodayStoriesContent(
    state: TodayStoriesState,
    modifier: Modifier = Modifier,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNextGroup: () -> Unit = {},
    onPreviousGroup: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val animationsDisabled = rememberAnimationsDisabled()

    AnimatedContent(
        targetState = state,
        contentKey = { it.group?.key },
        transitionSpec = {
            if (animationsDisabled) {
                return@AnimatedContent EnterTransition.None togetherWith ExitTransition.None
            }

            val forward = targetState.groupIndex >= initialState.groupIndex
            val direction = if (forward != isRtl) 1 else -1
            val spec = tween<IntOffset>(STORY_TRANSITION_MS)

            (slideInHorizontally(spec) { it * direction / 3 } + fadeIn(tween(STORY_TRANSITION_MS)))
                .togetherWith(
                    slideOutHorizontally(spec) { -it * direction / 3 } + fadeOut(tween(STORY_TRANSITION_MS)),
                )
        },
        label = "todayStory",
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .storySwipe(
                onNextGroup = onNextGroup,
                onPreviousGroup = onPreviousGroup,
            ),
    ) { story ->
        TodayStoryPage(
            state = story,
            isActive = transition.targetState == EnterExitState.Visible,
            onNext = onNext,
            onPrevious = onPrevious,
            onClose = onClose,
        )
    }
}

/** One story; [isActive] is false while it animates out, so its timer stops. */
@Composable
private fun TodayStoryPage(
    state: TodayStoriesState,
    isActive: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
) {
    val group = state.group ?: return
    val frame = state.frame ?: return

    var isFlipped by remember(frame.key) { mutableStateOf(false) }
    var isHeld by remember { mutableStateOf(false) }
    var isConfirming by remember { mutableStateOf(false) }

    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val isPaused = !isActive || isHeld || isFlipped || isConfirming || !lifecycle.isAtLeast(Lifecycle.State.RESUMED)

    val background = TraktTheme.colors.backgroundPrimary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
    ) {
        AsyncImage(
            model = frame.media.images?.getPosterUrl(Images.Size.MEDIUM),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(BACKDROP_ALPHA)
                .blur(BackdropBlur),
        )

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
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                ),
        ) {
            TodayStoryProgress(
                frameKeys = group.frames.map { it.key },
                currentIndex = state.frameIndex,
                isPaused = isPaused,
                onFinished = onNext,
            )

            TodayStoryHeader(
                title = when (group) {
                    is TodayStoryGroup.Title -> group.media.title
                    is TodayStoryGroup.ForYou -> stringResource(R.string.text_today_for_you)
                },
                position = stringResource(
                    R.string.text_today_story_position,
                    state.groupIndex + 1,
                    state.groups.size,
                ),
                onDetailsClick = { isFlipped = !isFlipped },
                onClose = onClose,
                modifier = Modifier.padding(bottom = PosterTopGap - 16.dp),
            )

            TodayStoryFrameContent(
                frame = frame,
                isFlipped = isFlipped && frame.key == frame.key,
                onFlip = { isFlipped = !isFlipped },
                onNext = onNext,
                onPrevious = onPrevious,
                onHeldChange = { isHeld = it },
                onConfirmVisibleChange = { isConfirming = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F),
            )
        }
    }
}

@Composable
private fun Modifier.storySwipe(
    onNextGroup: () -> Unit,
    onPreviousGroup: () -> Unit,
): Modifier {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val nextGroup by rememberUpdatedState(onNextGroup)
    val previousGroup by rememberUpdatedState(onPreviousGroup)

    return pointerInput(isRtl) {
        var total = 0F

        detectHorizontalDragGestures(
            onDragStart = { total = 0F },
            onDragEnd = {
                if (abs(total) < SwipeThreshold.toPx()) return@detectHorizontalDragGestures

                val towardsStart = (total > 0) != isRtl
                if (towardsStart) previousGroup() else nextGroup()
            },
            onHorizontalDrag = { change, amount ->
                change.consume()
                total += amount
            },
        )
    }
}

@Composable
private fun TodayStoryProgress(
    frameKeys: List<String>,
    currentIndex: Int,
    isPaused: Boolean,
    onFinished: () -> Unit,
) {
    val finished by rememberUpdatedState(onFinished)
    val currentKey = frameKeys.getOrNull(currentIndex)
    val progress = remember(currentKey) { Animatable(0F) }
    val animationsDisabled = rememberAnimationsDisabled()

    LaunchedEffect(currentKey, isPaused) {
        if (isPaused) return@LaunchedEffect

        val remaining = ((1F - progress.value) * FRAME_DURATION_MS).toLong()

        // With animations off a tween ends instantly, so time the frame with a plain delay.
        if (animationsDisabled) {
            val startedAt = SystemClock.uptimeMillis()
            val startProgress = progress.value
            try {
                delay(remaining)
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
        modifier = Modifier.fillMaxWidth(),
    ) {
        frameKeys.forEachIndexed { index, key ->
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
    title: String,
    position: String,
    onDetailsClick: () -> Unit,
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

        Column(
            modifier = Modifier.onClick(onClick = onClose),
        ) {
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

        Spacer(modifier = Modifier.weight(1F))

        TodayStoryHeaderButton(
            iconRes = R.drawable.ic_info,
            onClick = onDetailsClick,
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

        layout(placeable.width - startPx - endPx, placeable.height) {
            placeable.placeRelative(-startPx, 0)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val story = TodayTitleStory(
        media = TodayMedia.ShowMedia(PreviewData.show1),
        actions = persistentListOf(),
        users = persistentListOf(PreviewData.user1, PreviewData.user1),
        averageRating = 8F,
        milestone = null,
        latestAt = Instant.now(),
    )

    TraktTheme {
        TodayStoriesContent(
            state = TodayStoriesState(
                groups = TodayStoryGroup.from(
                    forYou = emptyList(),
                    stories = listOf(story),
                ),
            ),
            modifier = Modifier.background(Color.Black),
        )
    }
}
