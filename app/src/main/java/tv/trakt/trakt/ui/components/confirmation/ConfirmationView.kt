package tv.trakt.trakt.ui.components.confirmation

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ConfirmationView(
    title: String,
    message: String,
    yesText: String = stringResource(R.string.button_text_yes),
    noText: String = stringResource(R.string.button_text_cancel),
    onYes: () -> Unit = {},
    onNo: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = TraktTheme.typography.heading6,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier
                .padding(bottom = 30.dp),
        )

        Text(
            text = message,
            style = TraktTheme.typography.paragraph,
            color = TraktTheme.colors.textPrimary,
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
                text = yesText,
                onClick = onYes,
                modifier = Modifier.fillMaxWidth(),
            )

            PrimaryButton(
                text = noText,
                containerColor = TraktTheme.colors.primaryButtonContainerDisabled,
                onClick = onNo,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        ConfirmationView(
            title = "Delete list",
            message = "This action cannot be undone.",
        )
    }
}
