@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.CommentUserStats
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.theme.TraktTheme

private val TooltipShape = RoundedCornerShape(8.dp)

@Composable
internal fun CommentUserChips(
    comment: Comment,
    progressVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val rating = comment.user5Rating
    if (!progressVisible && rating == null) return

    val textStyle = TraktTheme.typography.meta.copy(fontWeight = W700)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = modifier,
    ) {
        if (progressVisible) {
            CommentProgressChip(
                stats = comment.userStats,
                textStyle = textStyle,
            )
        }

        if (rating != null) {
            InfoChip(
                text = rating,
                iconPainter = painterResource(R.drawable.ic_star_trakt_on),
                iconPadding = 1.dp,
                contentTextStyle = textStyle,
            )
        }
    }
}

@Composable
private fun CommentProgressChip(
    stats: CommentUserStats,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)

    TooltipBox(
        state = tooltipState,
        positionProvider = rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above,
        ),
        tooltip = {
            Text(
                text = stringResource(
                    R.string.tooltip_text_watched_episodes,
                    stats.completedCount,
                    stats.playCount,
                ),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.tooltipContent,
                modifier = Modifier
                    .background(TraktTheme.colors.tooltipContainer, TooltipShape)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        },
        // Focusable so a tap on the chip while shown only dismisses instead of also reopening.
        focusable = true,
        enableUserInput = false,
        modifier = modifier,
    ) {
        InfoChip(
            text = stats.completedPercent,
            iconPainter = painterResource(R.drawable.ic_eye),
            iconPadding = 2.dp,
            contentTextStyle = textStyle,
            modifier = Modifier.onClick {
                if (tooltipState.isVisible) {
                    tooltipState.dismiss()
                    return@onClick
                }
                scope.launch {
                    tooltipState.show()
                }
            },
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        CommentUserChips(
            comment = PreviewData.comment1.copy(userRating = 8),
            progressVisible = true,
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewRatingOnly() {
    TraktTheme {
        CommentUserChips(
            comment = PreviewData.comment1.copy(userRating = 8),
            progressVisible = false,
        )
    }
}
