@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.reactions.ui

import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.common.ui.theme.colors.Shade800
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
fun ReactionsSummaryToolTip(
    state: TooltipState,
    reactions: ReactionsSummary?,
    userReaction: Reaction? = null,
    onReactionClick: ((Reaction) -> Unit)? = null,
    contentAnchor: @Composable () -> Unit,
) {
    TooltipBox(
        state = state,
        content = contentAnchor,
        positionProvider = rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above,
            spacingBetweenTooltipAndAnchor = 2.dp,
        ),
        tooltip = {
            TooltipContent(
                reactions = reactions,
                userReaction = userReaction,
                onReactionClick = onReactionClick ?: {},
                onDismiss = {
                    state.dismiss()
                },
            )
        },
    )
}

@Composable
fun TooltipContent(
    modifier: Modifier = Modifier,
    reactions: ReactionsSummary?,
    userReaction: Reaction?,
    onReactionClick: (Reaction) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val summaryVisible = (userReaction != null)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier
            .widthIn(max = 272.dp)
            .dropShadow(
                shape = RoundedCornerShape(20.dp),
                shadow = Shadow(
                    radius = 4.dp,
                    color = Color.Black,
                    spread = 4.dp,
                    alpha = 0.1f,
                ),
            )
            .background(Shade800, RoundedCornerShape(20.dp))
            .padding(4.dp)
            .padding(bottom = 6.dp)
            .animateContentSize(
                alignment = Alignment.BottomStart,
                animationSpec = tween(200),
            ),
    ) {
        if (summaryVisible) {
            ReactionsSummaryGrid(
                reactions = reactions,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .onClick(onClick = onDismiss),
            )
        }

        ReactionsStrip(
            selectedReaction = userReaction,
            onReactionClick = onReactionClick,
            onCloseClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 4.dp,
                    end = 8.dp,
                    top = 6.dp,
                ),
        )
    }
}

@Preview()
@Composable
private fun Preview() {
    TraktTheme {
        TooltipContent(
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
        TooltipContent(
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
