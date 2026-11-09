package tv.trakt.trakt.core.userprofile.sections.match.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(4.dp),
        ) {
            Sparkle(color = TraktTheme.colors.textPrimary)
            Text(
                text = lead.name.capitalize(),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading1.copy(
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.01F).em,
                ),
                textAlign = TextAlign.Center,
            )
        }

        if (runnersUp.isNotEmpty()) {
            DottedTopics(
                names = runnersUp.map { it.name.capitalize() },
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading3.copy(
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                gap = 8.dp,
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
                gap = 8.dp,
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
    gap: Dp,
) {
    FlowRow(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth(),
    ) {
        names.forEachIndexed { index, name ->
            Row {
                if (index > 0) {
                    Text(
                        text = "•",
                        color = color,
                        style = style.copy(fontWeight = FontWeight.Normal),
                        modifier = Modifier.padding(horizontal = gap),
                    )
                }
                Text(
                    text = name,
                    color = color,
                    style = style,
                )
            }
        }
    }
}

@Composable
private fun Sparkle(color: Color) {
    Icon(
        painter = painterResource(R.drawable.ic_discover_off),
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(26.dp),
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        MatchTopics(topics = UserMatchPreviewData.match.sharedTopics)
    }
}
