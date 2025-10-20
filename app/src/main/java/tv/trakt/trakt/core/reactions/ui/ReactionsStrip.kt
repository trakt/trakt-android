package tv.trakt.trakt.core.reactions.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.EmojiSupportMatch
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
fun ReactionsStrip(
    modifier: Modifier = Modifier,
    selectedReaction: Reaction? = null,
    onCloseClick: (() -> Unit)? = null,
    onReactionClick: ((Reaction) -> Unit)? = null,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .onClick(onClick = onCloseClick ?: {})
                .size(17.dp)
                .graphicsLayer {
                    translationY = 1.dp.toPx()
                },
        )

        for (reaction in Reaction.entries) {
            val animatedAlpha: Float by animateFloatAsState(
                targetValue = when (selectedReaction) {
                    null, reaction -> 1f
                    else -> 0.25f
                },
                animationSpec = tween(150),
                label = "alpha",
            )

            val animatedScale by animateFloatAsState(
                targetValue = if (reaction == selectedReaction) 1F else 0.9f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )

            Text(
                text = reaction.emoji,
                fontSize = 16.sp,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        emojiSupportMatch = EmojiSupportMatch.Default,
                    ),
                ),
                modifier = Modifier
                    .alpha(animatedAlpha)
                    .scale(animatedScale)
                    .onClick(
                        onClick = { onReactionClick?.invoke(reaction) },
                    ),
            )
        }
    }
}

@Preview(widthDp = 300)
@Composable
private fun Preview2() {
    TraktTheme {
        ReactionsStrip()
    }
}

@Preview(widthDp = 300)
@Composable
private fun Preview() {
    TraktTheme {
        ReactionsStrip(
            selectedReaction = Reaction.LOVE,
        )
    }
}
