package tv.trakt.trakt.core.reactions.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.EmojiSupportMatch
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.extensions.thousandsFormat
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
fun ReactionsSummaryChip(
    modifier: Modifier = Modifier,
    reactions: ReactionsSummary? = null,
    userReaction: Reaction? = null,
    enabled: Boolean = true,
) {
    Row(
        horizontalArrangement = spacedBy(5.dp),
        verticalAlignment = CenterVertically,
        modifier = modifier,
    ) {
        if (enabled) {
            Icon(
                painter = when {
                    userReaction != null -> painterResource(R.drawable.ic_reaction_edit)
                    else -> painterResource(R.drawable.ic_reaction_add)
                },
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(18.dp),
            )
        }

        AnimatedVisibility(
            visible = reactions != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            reactions?.let { reactions ->
                val topReactions = remember(reactions) {
                    reactions.distribution.entries
                        .sortedByDescending { it.value }
                        .filter { it.value > 0 }
                        .take(3)
                        .map { it.key }
                }

                val totalCount = remember(reactions.reactionsCount) {
                    reactions.reactionsCount.thousandsFormat()
                }

                Row(
                    horizontalArrangement = spacedBy(5.dp),
                    verticalAlignment = CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = spacedBy(2.dp),
                        verticalAlignment = CenterVertically,
                    ) {
                        for (reaction in topReactions) {
                            Text(
                                text = reaction.emoji,
                                fontSize = 14.sp,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        emojiSupportMatch = EmojiSupportMatch.Default,
                                    ),
                                ),
                            )
                        }
                    }

                    if (reactions.reactionsCount > 1) {
                        Text(
                            text = totalCount,
                            style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700),
                            color = TraktTheme.colors.textPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 300)
@Composable
private fun Preview() {
    TraktTheme {
        ReactionsSummaryChip(
            userReaction = Reaction.SPOILER,
            reactions = ReactionsSummary(
                reactionsCount = 14,
                distribution = mapOf(
                    Reaction.LOVE to 2,
                    Reaction.LAUGH to 12,
                ).toImmutableMap(),
            ),
        )
    }
}
