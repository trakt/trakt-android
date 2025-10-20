@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.reactions.ui

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
import androidx.compose.material3.TooltipDefaults
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
    summary: Boolean = false,
    contentAnchor: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above,
        ),
        tooltip = {
            TooltipContent(
                reactions = reactions,
                summary = summary,
                onDismiss = {
                    state.dismiss()
                },
            )
        },
        state = state,
        content = contentAnchor,
    )
}

@Composable
fun TooltipContent(
    modifier: Modifier = Modifier,
    reactions: ReactionsSummary?,
    summary: Boolean,
    onDismiss: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier
            .widthIn(max = 264.dp)
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
            .padding(bottom = 8.dp),
    ) {
        if (summary) {
            ReactionsSummaryGrid(
                reactions = reactions,
                modifier = Modifier
                    .fillMaxWidth()
                    .onClick(onClick = onDismiss),
            )
        }

        ReactionsStrip(
            onReactionClick = { onDismiss() },
            onCloseClick = { onDismiss() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 4.dp,
                    end = 8.dp,
                    top = when {
                        summary -> 2.dp
                        else -> 8.dp
                    },
                ),
        )
    }
}

@Preview()
@Composable
private fun Preview() {
    TraktTheme {
        TooltipContent(
            summary = false,
            reactions = ReactionsSummary(
                reactionsCount = 14,
                usersCount = 2,
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
            summary = true,
            reactions = ReactionsSummary(
                reactionsCount = 14,
                usersCount = 2,
                distribution = mapOf(
                    Reaction.LOVE to 2,
                    Reaction.LAUGH to 1,
                ).toImmutableMap(),
            ),
        )
    }
}
