@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.reactions.media.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.reactions.MediaReaction
import tv.trakt.trakt.common.model.reactions.MediaReactionsSummary
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MediaReactionsQuickBarDropdown(
    state: TooltipState,
    summary: MediaReactionsSummary,
    userReactions: ImmutableList<MediaReaction>,
    isLimitReached: Boolean,
    onReactionClick: (MediaReaction) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    anchor: @Composable () -> Unit,
) {
    TooltipBox(
        state = state,
        content = anchor,
        positionProvider = rememberInBoundsAbovePositionProvider(),
        // The anchor handles its own taps; long press opens the full picker instead.
        enableUserInput = false,
        tooltip = {
            MediaReactionsQuickBar(
                summary = summary,
                userReactions = userReactions,
                isLimitReached = isLimitReached,
                onReactionClick = onReactionClick,
                onMoreClick = onMoreClick,
            )
        },
        modifier = modifier,
    )
}

private val ScreenMargin = 16.dp
private val QuickEmojiFontSize = 22.sp
private val AnchorSpacing = 4.dp
private val SummarySpacing = 4.dp

// The default tooltip provider does not keep the popup inside the window.
@Composable
private fun rememberInBoundsAbovePositionProvider(): PopupPositionProvider {
    val density = LocalDensity.current
    return remember(density) {
        val margin = with(density) { ScreenMargin.roundToPx() }
        val spacing = with(density) { AnchorSpacing.roundToPx() }

        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                val centeredX = anchorBounds.center.x - popupContentSize.width / 2
                val maxX = (windowSize.width - popupContentSize.width - margin).coerceAtLeast(margin)
                val x = centeredX.coerceIn(margin, maxX)

                val above = anchorBounds.top - popupContentSize.height - spacing
                val y = if (above >= 0) above else anchorBounds.bottom + spacing

                return IntOffset(x, y)
            }
        }
    }
}

@Composable
internal fun MediaReactionsQuickBar(
    summary: MediaReactionsSummary,
    userReactions: ImmutableList<MediaReaction>,
    isLimitReached: Boolean,
    onReactionClick: (MediaReaction) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasSummary = summary.reactionsCount > 0

    Layout(
        content = {
            if (hasSummary) {
                MediaReactionsSummaryCard(
                    summary = summary,
                    userReactions = userReactions,
                    modifier = Modifier
                        .padding(bottom = 3.dp),
                )
            }

            QuickRow(
                userReactions = userReactions,
                isLimitReached = isLimitReached,
                onReactionClick = onReactionClick,
                onMoreClick = onMoreClick,
            )
        },
        modifier = modifier
            .dropShadow(
                shape = RoundedCornerShape(24.dp),
                shadow = Shadow(
                    radius = 3.dp,
                    color = Color.Black,
                    spread = 1.dp,
                    alpha = 0.06F,
                ),
            )
            .background(
                color = TraktTheme.colors.reactionsContainer,
                shape = RoundedCornerShape(24.dp),
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) { measurables, constraints ->
        // The summary takes the quick row's width, since its pager cannot size itself to content.
        val row = measurables.last().measure(constraints.copy(minWidth = 0, minHeight = 0))
        val card = measurables
            .takeIf { it.size > 1 }
            ?.first()
            ?.measure(Constraints.fixedWidth(row.width))
        val cardSpace = card?.let { it.height + SummarySpacing.roundToPx() } ?: 0

        layout(row.width, cardSpace + row.height) {
            card?.place(0, SummarySpacing.roundToPx() / 2)
            row.place(0, cardSpace)
        }
    }
}

@Composable
private fun QuickRow(
    userReactions: ImmutableList<MediaReaction>,
    isLimitReached: Boolean,
    onReactionClick: (MediaReaction) -> Unit,
    onMoreClick: () -> Unit,
) {
    Row(verticalAlignment = CenterVertically) {
        for (reaction in QuickMediaReactions) {
            MediaReactionEmoji(
                reaction = reaction,
                selected = reaction in userReactions,
                enabled = !isLimitReached,
                fontSize = QuickEmojiFontSize,
                // The default highlight matches this container, so step one shade up.
                highlightColor = TraktTheme.colors.reactionsSummaryContainer,
                onClick = { onReactionClick(reaction) },
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .onClick(onClick = onMoreClick),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = stringResource(R.string.button_label_popup_reactions),
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(name = "Empty")
@Composable
private fun PreviewEmpty() {
    TraktThemeLightDark {
        MediaReactionsQuickBar(
            summary = MediaReactionsSummary(),
            userReactions = persistentListOf(),
            isLimitReached = false,
            onReactionClick = {},
            onMoreClick = {},
        )
    }
}

@Preview(name = "Limit reached")
@Composable
private fun PreviewLimitReached() {
    TraktThemeLightDark {
        MediaReactionsQuickBar(
            summary = MediaReactionsSummary(),
            userReactions = persistentListOf(
                MediaReaction.HeartEyes,
                MediaReaction.Shocked,
                MediaReaction.Fire,
            ),
            isLimitReached = true,
            onReactionClick = {},
            onMoreClick = {},
        )
    }
}
