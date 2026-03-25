package tv.trakt.trakt.ui.components.buttons.lists

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun WatchlistButton(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
        modifier = modifier,
    ) {
        Icon(
            painter = when {
                checked -> painterResource(R.drawable.ic_bookmark_on)
                else -> painterResource(R.drawable.ic_bookmark_off)
            },
            contentDescription = null,
            tint = TraktTheme.colors.primaryButtonContent,
            modifier = Modifier
                .size(22.dp),
        )

        Text(
            text = stringResource(R.string.button_text_watchlist).uppercase(),
            color = TraktTheme.colors.primaryButtonContent,
            style = TraktTheme.typography.buttonSecondary,
            maxLines = 1,
            overflow = Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .weight(1F, false)
                .padding(vertical = 8.dp),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewChecked() {
    TraktTheme {
        WatchlistButton(checked = true)
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewUnchecked() {
    TraktTheme {
        WatchlistButton(checked = false)
    }
}
