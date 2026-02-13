package tv.trakt.trakt.ui.components.notifications

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun NotificationsRationaleView(
    modifier: Modifier = Modifier,
    onOk: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_bell),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .size(64.dp)
                .rotate(-20F),
        )

        Text(
            text = stringResource(R.string.header_get_notified),
            style = TraktTheme.typography.heading3,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier
                .padding(bottom = 12.dp),
        )

        Text(
            text = stringResource(R.string.header_notifications_rationale),
            style = TraktTheme.typography.paragraph,
            color = TraktTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 5,
            overflow = Ellipsis,
        )

        Column(
            verticalArrangement = spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 34.dp),
        ) {
            PrimaryButton(
                text = "OK",
                onClick = onOk,
                containerColor = TraktTheme.colors.primaryButtonContainer,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "us",
)
@Composable
private fun Preview() {
    TraktTheme {
        NotificationsRationaleView()
    }
}
