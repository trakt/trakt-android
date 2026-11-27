package tv.trakt.trakt.core.mixtape.results.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.mixtape.Mixtape.Suggestion
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.core.mixtape.model.MIXTAPE_SLOT_COUNT
import tv.trakt.trakt.core.mixtape.results.MixtapeResultsState
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

private const val LEAVE_DURATION_MS = 260
private const val TOAST_DURATION_MS = 2200L
private const val LEAVE_DISTANCE_RATIO = 1.2F
private const val BEHIND_SCALE = 0.94F
private const val BEHIND_ALPHA = 0.5F
private const val BACKDROP_ALPHA = 0.35F
private const val ACTION_SCALE_BOOST = 0.1F
private const val POSTER_RATIO = 2F / 3F

private val SwipeCommitDistance = 100.dp
private val BehindOffset = 14.dp
private val ActionSize = 56.dp
private val GoalSlotShape = RoundedCornerShape(6.dp)
private val GoalSlotWidth = 32.dp

@Immutable
private data class KeptToast(
    val title: String,
    val left: Int,
)

@Composable
internal fun MixtapeDeck(
    state: MixtapeResultsState,
    current: Suggestion,
    modifier: Modifier = Modifier,
    onKeep: () -> Unit = {},
    onSkip: () -> Unit = {},
    onOpen: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val windowWidth = LocalWindowInfo.current.containerSize.width
    val commitDistance = with(density) { SwipeCommitDistance.toPx() }

    val offsetX = remember(current.item.key) { Animatable(0F) }
    var leaving by remember(current.item.key) { mutableStateOf(false) }
    var skipping by remember(current.item.key) { mutableStateOf(false) }
    var flipped by remember(current.item.key) { mutableStateOf(false) }
    var toast by remember { mutableStateOf<KeptToast?>(null) }

    PreloadPosters(suggestions = state.suggestions)

    LaunchedEffect(toast) {
        if (toast == null) return@LaunchedEffect
        delay(TOAST_DURATION_MS.milliseconds)
        toast = null
    }

    fun commit(keep: Boolean) {
        if (leaving) return
        leaving = true
        skipping = !keep
        scope.launch {
            val direction = if (keep) 1 else -1
            offsetX.animateTo(
                targetValue = direction * windowWidth * LEAVE_DISTANCE_RATIO,
                animationSpec = tween(LEAVE_DURATION_MS),
            )
            if (keep) {
                val left = MIXTAPE_SLOT_COUNT - state.position - 1
                toast = KeptToast(current.item.title, left).takeIf { left > 0 }
                onKeep()
            } else {
                onSkip()
            }
        }
    }

    val keepStrength = (offsetX.value / commitDistance).coerceIn(0F, 1F)
    val skipStrength = (-offsetX.value / commitDistance).coerceIn(0F, 1F)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        DeckBackdrop(item = current.item)

        MixtapeDeckLayout(
            cardRatio = POSTER_RATIO,
            spacing = 20.dp,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding().plus(4.dp),
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().plus(20.dp),
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                ),
            header = {
                DeckHeader(
                    state = state,
                    onBackClick = onBackClick,
                )
            },
            card = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(POSTER_RATIO, matchHeightConstraintsFirst = true),
                    ) {
                        BehindCard(
                            next = state.next?.item,
                            mystery = skipping || offsetX.value < 0F,
                            progress = abs(offsetX.value) / (windowWidth * LEAVE_DISTANCE_RATIO).coerceAtLeast(1F),
                            modifier = Modifier.fillMaxSize(),
                        )
                        MixtapeSwipeCard(
                            suggestion = current,
                            offsetX = offsetX.value,
                            commitDistance = commitDistance,
                            flipped = flipped,
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(current.item.key) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            when {
                                                offsetX.value > commitDistance -> commit(keep = true)
                                                offsetX.value < -commitDistance -> commit(keep = false)
                                                else -> scope.launch { offsetX.animateTo(0F, spring()) }
                                            }
                                        },
                                        onDragCancel = {
                                            scope.launch { offsetX.animateTo(0F, spring()) }
                                        },
                                    ) { change, amount ->
                                        if (leaving) return@detectHorizontalDragGestures
                                        change.consume()
                                        scope.launch { offsetX.snapTo(offsetX.value + amount) }
                                    }
                                }
                                .onClick {
                                    if (leaving) return@onClick
                                    flipped = !flipped
                                    if (flipped) onOpen()
                                },
                        )
                    }

                    toast?.let {
                        Text(
                            text = stringResource(R.string.text_mixtape_kept_toast, it.title, it.left),
                            style = TraktTheme.typography.meta,
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 12.dp)
                                .background(TraktTheme.colors.navigationContainer, RoundedCornerShape(100))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            },
            text = {
                DeckText(
                    suggestion = current,
                    becauseTitle = state.pickTitles[current.because],
                )
            },
            actions = {
                DeckActions(
                    keepStrength = keepStrength,
                    skipStrength = skipStrength,
                    onSkipClick = { commit(keep = false) },
                    onKeepClick = { commit(keep = true) },
                )
            },
        )
    }
}

@Composable
private fun PreloadPosters(suggestions: ImmutableList<Suggestion>) {
    val context = LocalPlatformContext.current

    LaunchedEffect(suggestions) {
        val loader = SingletonImageLoader.get(context)
        suggestions
            .flatMap { listOf(it.item.images?.getPosterUrl(), it.item.images?.getPosterUrl(Size.MEDIUM)) }
            .filterNotNull()
            .forEach { loader.enqueue(ImageRequest.Builder(context).data(it).build()) }
    }
}

@Composable
private fun DeckBackdrop(item: MixtapeItem) {
    val background = TraktTheme.colors.backgroundPrimary

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = item.images?.getPosterUrl(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = BACKDROP_ALPHA }
                .blur(40.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(background.copy(alpha = 0.4F), background),
                    ),
                ),
        )
    }
}

@Composable
private fun DeckHeader(
    state: MixtapeResultsState,
    onBackClick: () -> Unit,
) {
    val remaining = MIXTAPE_SLOT_COUNT - state.position

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .weight(1F)
                .onClick(onClick = onBackClick),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )
            TraktHeader(
                title = when {
                    remaining == 1 -> stringResource(R.string.header_mixtape_find_last)
                    state.isFirstRound -> stringResource(R.string.header_mixtape_find, MIXTAPE_SLOT_COUNT)
                    else -> stringResource(R.string.header_mixtape_find_more, MIXTAPE_SLOT_COUNT)
                },
                subtitle = when {
                    state.isFirstRound -> stringResource(
                        R.string.text_mixtape_kept,
                        state.position,
                        MIXTAPE_SLOT_COUNT,
                    )
                    else -> stringResource(
                        R.string.text_mixtape_round_kept,
                        state.position,
                        MIXTAPE_SLOT_COUNT,
                        state.keptSoFar,
                    )
                },
            )
        }

        GoalSlots(round = state.round)
    }
}

@Composable
private fun GoalSlots(round: ImmutableList<MixtapeItem>) {
    Row(horizontalArrangement = spacedBy(6.dp)) {
        repeat(MIXTAPE_SLOT_COUNT) { index ->
            val kept = round.getOrNull(index)
            val borderColor = when (index) {
                round.size -> TraktTheme.colors.accent
                else -> TraktTheme.colors.chipContainer
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = GoalSlotWidth, height = GoalSlotWidth / POSTER_RATIO)
                    .clip(GoalSlotShape)
                    .border(1.dp, borderColor, GoalSlotShape),
            ) {
                if (kept != null) {
                    AsyncImage(
                        model = kept.images?.getPosterUrl(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_bookmark_off),
                        contentDescription = null,
                        tint = TraktTheme.colors.textSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BehindCard(
    next: MixtapeItem?,
    mystery: Boolean,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val reveal = progress.coerceIn(0F, 1F)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                val scale = lerp(BEHIND_SCALE, 1F, reveal)
                scaleX = scale
                scaleY = scale
                translationY = lerp(BehindOffset.toPx(), 0F, reveal)
            }
            .clip(SwipeCardShape)
            .background(TraktTheme.colors.navigationContainer),
    ) {
        if (next != null && !mystery) {
            AsyncImage(
                model = next.images?.getPosterUrl(Size.MEDIUM),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = lerp(BEHIND_ALPHA, 1F, reveal) },
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_mixtape),
                contentDescription = null,
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

@Composable
private fun DeckText(
    suggestion: Suggestion,
    becauseTitle: String?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        becauseTitle?.let {
            Text(
                text = stringResource(R.string.text_mixtape_because, it).uppercase(),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.accentPalette.shade400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = suggestion.item.title,
            style = TraktTheme.typography.heading3,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = rememberMixtapeMeta(suggestion.item),
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DeckActions(
    keepStrength: Float,
    skipStrength: Float,
    onSkipClick: () -> Unit,
    onKeepClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        DeckAction(
            icon = R.drawable.ic_close,
            label = stringResource(R.string.button_text_mixtape_another),
            filled = false,
            scale = 1F + skipStrength * ACTION_SCALE_BOOST,
            onClick = onSkipClick,
        )

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(8.dp),
            modifier = Modifier.height(ActionSize),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { rotationZ = 180F },
            )
            Text(
                text = stringResource(R.string.text_mixtape_or_swipe),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textSecondary,
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier.size(16.dp),
            )
        }

        DeckAction(
            icon = R.drawable.ic_bookmark_on,
            label = stringResource(R.string.button_text_mixtape_keep),
            filled = true,
            scale = 1F + keepStrength * ACTION_SCALE_BOOST,
            onClick = onKeepClick,
        )
    }
}

@Composable
private fun DeckAction(
    @DrawableRes icon: Int,
    label: String,
    filled: Boolean,
    scale: Float,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(6.dp),
        modifier = Modifier.width(ActionSize),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(ActionSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(if (filled) TraktTheme.colors.accent else Color.Transparent)
                .border(1.dp, if (filled) Color.Transparent else TraktTheme.colors.chipContainer, CircleShape)
                .onClick(onClick = onClick),
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (filled) TraktTheme.colors.textPrimaryOnAccent else TraktTheme.colors.textPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = label,
            style = TraktTheme.typography.meta,
            color = if (filled) TraktTheme.colors.accent else TraktTheme.colors.textSecondary,
            maxLines = 1,
            modifier = Modifier.wrapContentWidth(unbounded = true),
        )
    }
}

private val previewSuggestions = persistentListOf(
    Suggestion(
        item = MixtapeItem.ShowItem(PreviewData.show1),
        because = PreviewData.show2.ids.trakt,
        match = 88,
    ),
    Suggestion(
        item = MixtapeItem.MovieItem(PreviewData.movie1),
        because = PreviewData.show2.ids.trakt,
        match = 81,
    ),
    Suggestion(
        item = MixtapeItem.MovieItem(PreviewData.movie2),
        because = PreviewData.show2.ids.trakt,
        match = 74,
    ),
)

private val previewState = MixtapeResultsState(
    loading = false,
    suggestions = previewSuggestions,
    slots = previewSuggestions,
    pickTitles = persistentMapOf(PreviewData.show2.ids.trakt to PreviewData.show2.title),
)

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        MixtapeDeck(
            state = previewState,
            current = previewSuggestions[0],
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun KeptPreview() {
    TraktThemePreview {
        MixtapeDeck(
            state = previewState.copy(
                rounds = persistentListOf(persistentListOf(previewSuggestions[0].item)),
            ),
            current = previewSuggestions[1],
        )
    }
}
