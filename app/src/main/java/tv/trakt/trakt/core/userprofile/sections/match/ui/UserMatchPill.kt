package tv.trakt.trakt.core.userprofile.sections.match.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberPercentFormat
import tv.trakt.trakt.common.model.UserMatch
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val PillShape = RoundedCornerShape(100)
private val PillHeight = 30.dp
private val FillInset = 2.5.dp

@Composable
internal fun UserMatchPill(
    match: UserMatch,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        contentAlignment = CenterStart,
        modifier = modifier
            .onClick(onClick = onClick),
    ) {
        MatchPillContent(score = match.score)
    }
}

@Composable
private fun MatchPillContent(
    score: Int,
    modifier: Modifier = Modifier,
) {
    val alpha = 0.8F
    val containerColor = TraktTheme.colors.chipContainer
        .copy(alpha = alpha)
    val fillColor = TraktTheme.colors.chipProgress
        .copy(alpha = alpha - 0.2F)
    val contentColor = TraktTheme.colors.chipContent
    val fill = score / 100F

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = modifier
            .height(PillHeight)
            .clip(PillShape)
            .background(containerColor, PillShape)
            .drawBehind {
                if (fill <= 0F) return@drawBehind

                val inset = FillInset.toPx()
                val height = size.height - inset * 2
                val width = ((size.width - inset * 2) * fill).coerceAtLeast(height)
                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(inset, inset),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(height / 2),
                )
            }
            .padding(
                start = 10.dp,
                end = 6.dp,
            ),
    ) {
        Text(
            text = stringResource(R.string.match_pill_anchor),
            color = contentColor,
            style = TraktTheme.typography.meta.copy(
                fontWeight = W400,
                lineHeight = 1.2.em,
            ),
            maxLines = 1,
        )

        Text(
            text = score.rememberPercentFormat(),
            color = contentColor,
            style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = W700),
            maxLines = 1,
        )

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier
                .size(12.dp),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            listOf(92, 55, 12).forEach { score ->
                UserMatchPill(
                    match = UserMatchPreviewData.match.copy(score = score),
                )
            }
        }
    }
}
