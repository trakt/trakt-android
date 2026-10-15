package tv.trakt.trakt.core.today.pill

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.helpers.extensions.rememberAnimationsDisabled
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val PillSize = 34.dp
private val RingWidth = 1.1.dp

private val PosterWidth = 13.dp
private val PosterHeight = 18.dp
private val PosterOffset = 2.5.dp
private val PosterCorner = 2.dp
private const val POSTER_TILT = 10F

private val BadgeSize = 13.dp
private val BadgeOffset = 1.5.dp
private val BadgePadding = 3.5.dp

private const val POP_CYCLE_MS = 8_000
private const val POP_DELAY_MS = 3000L
private const val POP_COUNT = 3
private const val POP_SCALE = 1.15F

@Composable
internal fun TodayPill(
    modifier: Modifier = Modifier,
    viewModel: TodayPillViewModel = koinViewModel(),
    onClick: (groupKey: String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (!state.isVisible) return

    TodayPillContent(
        posters = state.posters,
        unseenCount = state.unseenCount,
        onClick = { state.targetKey?.let(onClick) },
        modifier = modifier,
    )
}

@Composable
internal fun TodayPillContent(
    posters: ImmutableList<String>,
    unseenCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val unseen = unseenCount > 0
    val animate = unseen && !rememberAnimationsDisabled()

    val ringColor by animateColorAsState(
        targetValue = when {
            unseen -> TraktTheme.colors.accent
            else -> TraktTheme.colors.chipContainer
        },
        animationSpec = tween(durationMillis = 200),
        label = "todayPillRing",
    )

    // Keeps the last count on screen while the badge fades out after everything is seen.
    val lastCount = remember { mutableIntStateOf(unseenCount) }
    SideEffect {
        if (unseen) lastCount.intValue = unseenCount
    }
    val badgeCount = if (unseen) unseenCount else lastCount.intValue

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(PillSize)
            .onClick(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = RingWidth,
                    color = ringColor,
                    shape = CircleShape,
                ),
        )

        TodayPillPosters(
            posters = posters,
            modifier = Modifier
                .padding(RingWidth)
                .clip(CircleShape)
                .fillMaxSize(),
        )

        AnimatedVisibility(
            visible = unseen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = BadgeOffset, y = -BadgeOffset),
        ) {
            TodayPillBadge(
                count = badgeCount,
                animate = animate,
            )
        }
    }
}

@Composable
private fun TodayPillPosters(
    posters: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        posters.forEachIndexed { index, url ->
            val direction = when {
                posters.size < 2 -> 0
                index == 0 -> -1
                else -> 1
            }

            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = PosterWidth, height = PosterHeight)
                    .graphicsLayer {
                        translationX = direction * PosterOffset.toPx()
                        rotationZ = direction * POSTER_TILT
                    }
                    .clip(RoundedCornerShape(PosterCorner))
                    .background(TraktTheme.colors.placeholderContainer),
            )
        }
    }
}

@Composable
private fun TodayPillBadge(
    count: Int,
    animate: Boolean,
) {
    val scale = remember { Animatable(1F) }

    LaunchedEffect(count, animate) {
        if (!animate) {
            scale.snapTo(1F)
            return@LaunchedEffect
        }

        delay(POP_DELAY_MS)
        repeat(POP_COUNT) {
            scale.animateTo(
                targetValue = 1F,
                animationSpec = keyframes {
                    durationMillis = POP_CYCLE_MS
                    1F at (POP_CYCLE_MS * 0.70F).toInt()
                    POP_SCALE at (POP_CYCLE_MS * 0.75F).toInt()
                    1F at (POP_CYCLE_MS * 0.82F).toInt()
                },
            )
        }
    }

    val shape = RoundedCornerShape(50)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .heightIn(min = BadgeSize)
            .widthIn(min = BadgeSize)
            .background(TraktTheme.colors.accent, shape)
            .padding(horizontal = BadgePadding),
    ) {
        Text(
            text = count.toString(),
            style = TraktTheme.typography.meta.copy(
                fontSize = 10.sp,
            ),
            color = TraktTheme.colors.textPrimaryOnAccent,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Row(
            modifier = Modifier
                .background(Color(0xFF131517))
                .padding(16.dp),
        ) {
            TodayPillContent(
                posters = persistentListOf("a", "b"),
                unseenCount = 7,
                onClick = {},
            )
            TodayPillContent(
                posters = persistentListOf("a"),
                unseenCount = 12,
                modifier = Modifier.padding(start = 24.dp),
                onClick = {},
            )
            TodayPillContent(
                posters = persistentListOf("a", "b"),
                unseenCount = 0,
                modifier = Modifier.padding(start = 24.dp),
                onClick = {},
            )
        }
    }
}
