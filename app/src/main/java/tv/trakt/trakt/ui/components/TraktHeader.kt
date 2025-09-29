package tv.trakt.trakt.ui.components

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun TraktHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        verticalArrangement = spacedBy(1.dp, Alignment.CenterVertically),
        modifier = modifier,
    ) {
        Text(
            text = title,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            maxLines = 1,
            overflow = Ellipsis,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta.copy(fontWeight = W400),
                maxLines = 1,
                overflow = Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        TraktHeader(
            title = "Title",
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        TraktHeader(
            title = "Title",
            subtitle = "Subtitle",
        )
    }
}
