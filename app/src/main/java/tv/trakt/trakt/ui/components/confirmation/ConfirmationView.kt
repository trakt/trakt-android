package tv.trakt.trakt.ui.components.confirmation

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.buttons.PrimaryHoldButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ConfirmationView(
    title: String,
    message: String? = null,
    annotatedMessage: AnnotatedString? = null,
    holdToYes: Boolean = false,
    yesColor: Color = TraktTheme.colors.primaryButtonContainer,
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

        annotatedMessage?.let {
            Text(
                text = it,
                style = TraktTheme.typography.paragraph,
                color = TraktTheme.colors.textPrimary,
                maxLines = 5,
                overflow = Ellipsis,
            )
        } ?: Text(
            text = message ?: "",
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
            if (holdToYes) {
                PrimaryHoldButton(
                    text = yesText,
                    onClick = onYes,
                    containerColor = yesColor,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                PrimaryButton(
                    text = yesText,
                    onClick = onYes,
                    containerColor = yesColor,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

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
    locale = "us",
)
@Composable
private fun Preview() {
    TraktTheme {
        val res = LocalResources.current
        val string = res.getString(
            R.string.warning_prompt_mark_as_watched_show_android,
            12,
            "Title",
        )

        ConfirmationView(
            title = "Delete list",
            message = "This action cannot be undone.",
            annotatedMessage = AnnotatedString.fromHtml(string),
            holdToYes = true,
        )
    }
}
