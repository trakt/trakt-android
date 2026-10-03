package tv.trakt.trakt.core.reactions.media.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.EmojiSupportMatch
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.ifOrElse
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.ui.theme.TraktTheme

internal val QuickMediaReactions: ImmutableList<MediaReaction> = persistentListOf(
    MediaReaction.HeartEyes,
    MediaReaction.Rofl,
    MediaReaction.HoldingBackTears,
    MediaReaction.MindBlown,
    MediaReaction.Shocked,
    MediaReaction.Yawning,
)

// English-only until the reaction names are localised.
internal val MediaReaction.displayName: String
    get() = value
        .replace('_', ' ')
        .replaceFirstChar { it.uppercase() }

@Composable
internal fun MediaReactionEmoji(
    reaction: MediaReaction,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    size: Dp = 40.dp,
    fontSize: TextUnit = 24.sp,
    highlightColor: Color = TraktTheme.colors.reactionsSummaryHighlight,
    onClick: (() -> Unit)? = null,
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (enabled || selected) 1F else 0.3F,
        animationSpec = tween(150),
        label = "alpha",
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1F else 0.9F,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "scale",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(
                color = when {
                    selected -> highlightColor
                    else -> Color.Transparent
                },
                shape = CircleShape,
            )
            .alpha(animatedAlpha)
            .semantics {
                contentDescription = reaction.displayName
                this.selected = selected
            }
            .ifOrElse(
                condition = onClick != null,
                isTrue = Modifier.onClick(
                    enabled = enabled || selected,
                    throttle = false,
                    onClick = onClick ?: {},
                ),
            ),
    ) {
        Text(
            text = reaction.emoji,
            fontSize = fontSize,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    emojiSupportMatch = EmojiSupportMatch.Default,
                ),
            ),
            modifier = Modifier.scale(animatedScale),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktThemeLightDark {
        Row {
            MediaReactionEmoji(reaction = MediaReaction.Popcorn)
            MediaReactionEmoji(reaction = MediaReaction.Fire, selected = true)
            MediaReactionEmoji(reaction = MediaReaction.Skull, enabled = false)
        }
    }
}
