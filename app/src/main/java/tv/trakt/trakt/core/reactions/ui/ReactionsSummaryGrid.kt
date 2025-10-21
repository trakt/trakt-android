package tv.trakt.trakt.core.reactions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import tv.trakt.trakt.common.ui.theme.colors.Shade700
import tv.trakt.trakt.common.ui.theme.colors.Shade800
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val firstRowReactions = listOf(
    Reaction.LIKE,
    Reaction.DISLIKE,
    Reaction.LOVE,
    Reaction.LAUGH,
)

private val secondRowReactions = listOf(
    Reaction.SHOCKED,
    Reaction.BRAVO,
    Reaction.SPOILER,
)

@Composable
fun ReactionsSummaryGrid(
    modifier: Modifier = Modifier,
    reactions: ReactionsSummary?,
    userReaction: Reaction?,
) {
    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(
                Shade700,
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp,
                ),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reaction_dot),
                contentDescription = null,
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier
                    .size(18.dp),
            )

            Text(
                text = stringResource(R.string.text_comments_reactions).uppercase(),
                style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700, fontSize = 12.sp),
                color = TraktTheme.colors.textSecondary,
            )
        }

        reactions?.let {
            Column(
                verticalArrangement = spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    for (reaction in firstRowReactions) {
                        ReactionItem(
                            reaction = reaction to (reactions.distribution[reaction] ?: 0),
                            highlight = reaction == userReaction,
                        )
                    }
                }

                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    for (reaction in secondRowReactions) {
                        ReactionItem(
                            reaction = reaction to (reactions.distribution[reaction] ?: 0),
                            highlight = reaction == userReaction,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReactionItem(
    modifier: Modifier = Modifier,
    reaction: Pair<Reaction, Int>,
    highlight: Boolean,
) {
    Row(
        horizontalArrangement = spacedBy(4.dp),
        verticalAlignment = CenterVertically,
        modifier = modifier
            .background(
                if (highlight) Shade800 else Color.Transparent,
                RoundedCornerShape(12.dp),
            )
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp,
            ),
    ) {
        Text(
            text = reaction.first.emoji,
            fontSize = 14.sp,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    emojiSupportMatch = EmojiSupportMatch.Default,
                ),
            ),
        )

        Text(
            text = reaction.second.thousandsFormat(),
            style = TraktTheme.typography.paragraphSmall.copy(fontWeight = W700),
            color = TraktTheme.colors.textPrimary,
        )
    }
}

@Preview(widthDp = 300)
@Composable
private fun Preview() {
    TraktTheme {
        ReactionsSummaryGrid(
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

@Preview(widthDp = 300)
@Composable
private fun Preview2() {
    TraktTheme {
        ReactionsSummaryGrid(
            userReaction = Reaction.LOVE,
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
