package tv.trakt.trakt.core.home.sections.activity.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState.Visible
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.ratings.ui.UserRatingBar
import tv.trakt.trakt.core.ratings.ui.ratingDelight
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.FloatingContainerShadow
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

private val DismissDelay = 600.milliseconds
private val DismissDelightDelay = 2100.milliseconds

private val PopupShape = RoundedCornerShape(16.dp)
private val PopupGap = 4.dp
private val PopupMargin = 12.dp

// Room around the popup so the blurred shadow is not clipped by the popup window bounds.
private val PopupShadowInset = FloatingContainerShadow.radius * 2 + FloatingContainerShadow.spread

private val PopupIdleTopPadding = 16.dp
private val PopupDragTopPadding = 50.dp
private val PopupLabelSpacing = 40.dp

private data class RatingCommit(
    val id: Int,
    val rating: Int?,
)

@Composable
internal fun ActivityRateButton(
    rating: UserRating?,
    title: String,
    onRateClick: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
) {
    var popupVisible by remember { mutableStateOf(false) }
    var commit by remember { mutableStateOf<RatingCommit?>(null) }

    val rateDescription = stringResource(R.string.button_label_rate, title)
    val changeRatingDescription = stringResource(R.string.button_label_change_rating, title)

    LaunchedEffect(commit) {
        val current = commit ?: return@LaunchedEffect
        val hasDelight = current.rating?.let(::ratingDelight) != null
        delay(if (hasDelight) DismissDelightDelay else DismissDelay)
        popupVisible = false
    }

    val buttonModifier = Modifier
        .onClick { popupVisible = true }
        .padding(4.dp)

    Box(modifier = modifier) {
        when {
            rating != null -> ActivityItemRating(
                rating = rating,
                starSize = starSize,
                modifier = buttonModifier.semantics {
                    contentDescription = changeRatingDescription
                },
            )

            else -> Icon(
                painter = painterResource(R.drawable.ic_star_trakt_off),
                contentDescription = rateDescription,
                tint = TraktTheme.colors.textPrimary,
                modifier = buttonModifier.size(starSize),
            )
        }

        if (popupVisible) {
            RatingPopup(
                rating = rating?.rating,
                onDismiss = {
                    popupVisible = false
                    commit = null
                },
                onRateClick = {
                    onRateClick(it)
                    commit = RatingCommit(
                        id = (commit?.id ?: 0) + 1,
                        rating = it,
                    )
                },
            )
        }
    }
}

@Composable
internal fun ActivityItemRating(
    rating: UserRating,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(2.dp),
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_star_trakt_on),
            contentDescription = null,
            modifier = Modifier.size(starSize),
            tint = TraktTheme.colors.textPrimary,
        )
        Text(
            text = rating.rating5Scale,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.meta.copy(fontSize = 13.sp),
        )
    }
}

@Composable
private fun RatingPopup(
    rating: Int?,
    onDismiss: () -> Unit,
    onRateClick: (Int?) -> Unit,
) {
    val density = LocalDensity.current
    val positionProvider = remember(density) {
        AboveAnchorPositionProvider(
            gap = with(density) { (PopupGap - PopupShadowInset).roundToPx() },
            margin = with(density) { (PopupMargin - PopupShadowInset).roundToPx() },
        )
    }
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = scaleIn(
                animationSpec = tween(150),
                initialScale = 0.9F,
                transformOrigin = TransformOrigin(0.5F, 1F),
            ),
        ) {
            // ModulateAlpha fades without an offscreen layer, which would clip the shadow.
            val alpha by transition.animateFloat(
                transitionSpec = { tween(150) },
                label = "alpha",
            ) { if (it == Visible) 1F else 0F }

            RatingPopupContent(
                rating = rating,
                onRateClick = onRateClick,
                onDismiss = onDismiss,
                modifier = Modifier
                    .graphicsLayer {
                        this.alpha = alpha
                        compositingStrategy = CompositingStrategy.ModulateAlpha
                    }
                    .padding(PopupShadowInset),
            )
        }
    }
}

@Composable
private fun RatingPopupContent(
    rating: Int?,
    onRateClick: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    dragging: Boolean = false,
) {
    var isDragging by remember { mutableStateOf(dragging) }
    val backgroundInset by animateDpAsState(
        targetValue = when {
            isDragging -> 0.dp
            else -> PopupDragTopPadding - PopupIdleTopPadding
        },
        animationSpec = tween(150),
        label = "backgroundInset",
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = backgroundInset)
                .dropShadow(
                    shape = PopupShape,
                    shadow = FloatingContainerShadow,
                )
                .background(TraktTheme.colors.dialogContainer, PopupShape),
        )

        UserRatingBar(
            rating = rating,
            favoriteVisible = false,
            textSpacing = PopupLabelSpacing,
            topPadding = 0.dp,
            onRatingDrag = { isDragging = it },
            onRatingClick = onRateClick,
            onRatingRemoveClick = { onRateClick(null) },
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = PopupDragTopPadding,
                bottom = 16.dp,
            ),
        )

        // fillMaxWidth would stretch the popup to the window width.
        Box(
            modifier = Modifier
                .matchParentSize()
                .wrapContentHeight(Alignment.Top)
                .height(backgroundInset)
                .onClick(onClick = onDismiss),
        )
    }
}

private class AboveAnchorPositionProvider(
    private val gap: Int,
    private val margin: Int,
) : PopupPositionProvider {
    // Keeps the popup in place when the anchor content changes width after rating.
    private var lockedAnchor: IntRect? = null

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val anchor = lockedAnchor ?: anchorBounds.also { lockedAnchor = it }
        val centeredX = anchor.center.x - popupContentSize.width / 2
        val maxX = (windowSize.width - popupContentSize.width - margin).coerceAtLeast(margin)
        val y = anchor.top - popupContentSize.height - gap

        return IntOffset(
            x = centeredX.coerceIn(margin, maxX),
            y = y.coerceAtLeast(0),
        )
    }
}

@Preview
@Composable
private fun ActivityRateButtonPreview() {
    TraktTheme {
        Row(horizontalArrangement = spacedBy(16.dp)) {
            ActivityRateButton(
                rating = null,
                title = "",
                onRateClick = {},
            )
            ActivityRateButton(
                rating = UserRating(
                    mediaId = TraktId(1),
                    mediaType = MediaType.Movie,
                    rating = 7,
                ),
                title = "",
                onRateClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun RatingPopupContentPreview() {
    TraktTheme {
        RatingPopupContent(
            rating = null,
            onRateClick = {},
            modifier = Modifier.padding(PopupShadowInset),
        )
    }
}

@Preview
@Composable
private fun RatingPopupContentDraggingPreview() {
    TraktTheme {
        RatingPopupContent(
            rating = 7,
            onRateClick = {},
            modifier = Modifier.padding(PopupShadowInset),
            dragging = true,
        )
    }
}
