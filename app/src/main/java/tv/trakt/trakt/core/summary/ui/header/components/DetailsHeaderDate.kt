package tv.trakt.trakt.core.summary.ui.header.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.ui.theme.TraktTheme

/** Release date for the meta line. Upcoming titles get a highlighted date. */
@Composable
internal fun DetailsHeaderDate(
    text: String,
    isReleased: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = when {
            isReleased -> TraktTheme.colors.textSecondary
            else -> TraktTheme.colors.textPrimary
        },
        style = when {
            isReleased -> TraktTheme.typography.paragraphSmaller
            else -> TraktTheme.typography.paragraphSmaller.copy(fontWeight = FontWeight.W700)
        },
        maxLines = 1,
        modifier = modifier.padding(
            end = if (isReleased) 0.dp else 1.dp,
        ),
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Column {
            DetailsHeaderDate(text = "2024", isReleased = true)
            DetailsHeaderDate(text = "Dec 12, 2026", isReleased = false)
        }
    }
}
