package tv.trakt.trakt.app.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import tv.trakt.trakt.app.R
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun ConfirmationDialog(
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
    cancelText: String = stringResource(R.string.cancel),
    confirmText: String = stringResource(R.string.yes),
    onCancel: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    val (no, yes) = remember { FocusRequester.createRefs() }

    LaunchedEffect(Unit) {
        yes.requestFocus()
    }

    Column(
        modifier = modifier
            .background(
                shape = RoundedCornerShape(16.dp),
                color = TraktTheme.colors.dialogContainer,
            )
            .padding(24.dp),
    ) {
        Column(
            verticalArrangement = spacedBy(24.dp),
        ) {
            title?.let {
                Text(
                    text = title.uppercase(),
                    style = TraktTheme.typography.heading6,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            message?.let {
                Text(
                    text = message,
                    style = TraktTheme.typography.paragraph,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Row(
            horizontalArrangement = spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
                .focusGroup(),
        ) {
            PrimaryButton(
                text = cancelText,
                onClick = onCancel,
                containerColor = Color.White,
                contentColor = Color.Black,
                borderColor = TraktTheme.colors.accent,
                modifier = Modifier
                    .weight(1F)
                    .focusRequester(no),
            )

            PrimaryButton(
                text = confirmText,
                onClick = onConfirm,
                modifier = Modifier
                    .weight(2F)
                    .focusRequester(yes),
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview() {
    TraktTheme {
        ConfirmationDialog(
            title = "Mark as watched",
            message = "Do you really want to drop the mic from your \"Up Next\" ? " +
                "This will mark all of the episodes as watched.",
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview2() {
    TraktTheme {
        ConfirmationDialog(
            message = "Do you really want to drop the mic from your \"Up Next\" ?",
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview3() {
    TraktTheme {
        ConfirmationDialog(
            title = "Mark as watched",
        )
    }
}
