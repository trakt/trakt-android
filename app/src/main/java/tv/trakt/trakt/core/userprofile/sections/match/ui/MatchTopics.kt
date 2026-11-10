package tv.trakt.trakt.core.userprofile.sections.match.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.model.UserMatch
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SPARKLE_ID = "sparkle"

@Composable
internal fun MatchTopics(
    topics: ImmutableList<UserMatch.SharedTopic>,
    modifier: Modifier = Modifier,
) {
    val lead = topics.firstOrNull() ?: return
    val runnersUp = topics.drop(1).take(2)
    val supporting = topics.drop(3).take(3)

    Column(
        verticalArrangement = spacedBy(4.dp),
        horizontalAlignment = CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = buildAnnotatedString {
                appendInlineContent(SPARKLE_ID)
                append(" ")
                append(lead.name.capitalize())
            },
            inlineContent = sparkleContent(TraktTheme.colors.textPrimary),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading1.copy(
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.01F).em,
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 20.sp,
                maxFontSize = 32.sp,
                stepSize = 0.5.sp,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        if (runnersUp.isNotEmpty()) {
            DottedTopics(
                names = runnersUp.map { it.name.capitalize() },
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading3.copy(
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                minFontSize = 14.sp,
            )
        }

        if (supporting.isNotEmpty()) {
            DottedTopics(
                names = supporting.map { it.name.capitalize() },
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                ),
                minFontSize = 10.sp,
                modifier = Modifier
                    .padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun DottedTopics(
    modifier: Modifier = Modifier,
    names: List<String>,
    color: Color,
    style: TextStyle,
    minFontSize: TextUnit,
) {
    val text = buildAnnotatedString {
        names.forEachIndexed { index, name ->
            if (index > 0) {
                withStyle(SpanStyle(fontWeight = FontWeight.Normal)) {
                    append("  •  ")
                }
            }
            append(name)
        }
    }

    Text(
        text = text,
        color = color,
        style = style,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(
            minFontSize = minFontSize,
            maxFontSize = style.fontSize,
            stepSize = 0.5.sp,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

private fun sparkleContent(color: Color) =
    mapOf(
        SPARKLE_ID to InlineTextContent(
            Placeholder(
                width = 0.8.em,
                height = 0.8.em,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_discover_off),
                contentDescription = null,
                tint = color,
                modifier = Modifier.fillMaxSize(),
            )
        },
    )

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        MatchTopics(topics = UserMatchPreviewData.match.sharedTopics)
    }
}
