@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.reactions.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.common.ui.theme.colors.Shade800
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
fun ReactionsToolTip(
    state: TooltipState,
    reactions: ReactionsSummary?,
    modifier: Modifier = Modifier,
    userReaction: Reaction? = null,
    onReactionClick: ((Reaction) -> Unit)? = null,
    contentAnchor: @Composable () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    TooltipBox(
        state = state,
        content = contentAnchor,
        positionProvider = rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above,
        ),
        tooltip = {
            ReactionsToolTipContent(
                reactions = reactions,
                userReaction = userReaction,
                onReactionClick = {
                    onReactionClick?.invoke(it)
                    haptic.performHapticFeedback(Confirm)
                },
                onDismiss = {
                    state.dismiss()
                },
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun ReactionsToolTipContent(
    modifier: Modifier = Modifier,
    reactions: ReactionsSummary?,
    userReaction: Reaction?,
    onReactionClick: (Reaction) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val summaryVisible = true

    val animatedAlpha: Float by animateFloatAsState(
        targetValue = if (summaryVisible) 1f else 0f,
        animationSpec = tween(200),
        label = "alpha",
    )

    val animatedTranslation by animateIntAsState(
        targetValue = if (summaryVisible) 0 else 6,
        animationSpec = tween(200),
        label = "translation",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier
            .widthIn(max = 300.dp)
            .dropShadow(
                shape = RoundedCornerShape(20.dp),
                shadow = Shadow(
                    radius = 4.dp,
                    color = if (summaryVisible) Color.Black else Color.Transparent,
                    spread = 4.dp,
                    alpha = 0.1f,
                ),
            )
            .background(
                color = Shade800.copy(alpha = animatedAlpha),
                shape = RoundedCornerShape(20.dp),
            ),
    ) {
        ReactionsSummaryGrid(
            reactions = reactions,
            userReaction = userReaction,
            modifier = Modifier
                .alpha(animatedAlpha)
                .fillMaxWidth()
                .padding(
                    top = 6.dp,
                    bottom = 4.dp,
                    start = 6.dp,
                    end = 6.dp,
                )
                .graphicsLayer {
                    if (summaryVisible) {
                        translationY = animatedTranslation.dp.toPx()
                    }
                }
                .onClick(onClick = onDismiss),
        )

        ReactionsStrip(
            selectedReaction = userReaction,
            onReactionClick = onReactionClick,
            onCloseClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .dropShadow(
                    shape = RoundedCornerShape(20.dp),
                    shadow = Shadow(
                        radius = 4.dp,
                        color = if (summaryVisible) Color.Transparent else Color.Black,
                        spread = 4.dp,
                        alpha = 0.1f,
                    ),
                )
                .background(Shade800, RoundedCornerShape(20.dp))
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    top = 6.dp,
                    bottom = 6.dp,
                ),
        )
    }
}

@Preview()
@Composable
private fun Preview() {
    TraktTheme {
        ReactionsToolTipContent(
            userReaction = null,
            reactions = ReactionsSummary(
                reactionsCount = 14,
                distribution = mapOf(
                    Reaction.LOVE to 2,
                    Reaction.LAUGH to 1,
                ).toImmutableMap(),
            ),
        )
    }
}

@Preview()
@Composable
private fun Preview2() {
    TraktTheme {
        ReactionsToolTipContent(
            userReaction = Reaction.SPOILER,
            reactions = ReactionsSummary(
                reactionsCount = 14,
                distribution = mapOf(
                    Reaction.LOVE to 2,
                    Reaction.LAUGH to 1,
                ).toImmutableMap(),
            ),
        )
    }
}
