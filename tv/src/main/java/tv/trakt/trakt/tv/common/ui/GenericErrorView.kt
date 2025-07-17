import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import tv.trakt.trakt.tv.R
import tv.trakt.trakt.tv.ui.theme.TraktTheme

@Composable
internal fun GenericErrorView(
    error: Exception,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = "Warning",
            tint = TraktTheme.colors.accent,
            modifier = Modifier
                .size(72.dp)
                .focusRequester(focusRequester)
                .focusable(),
        )
        Text(
            text = stringResource(R.string.error_generic_title),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.error_generic_message),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.heading6,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Code: ${error.message ?: "Unknown error."}",
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.heading6,
            textAlign = TextAlign.Center,
            maxLines = 10,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        GenericErrorView(
            error = Exception("This is a test error"),
        )
    }
}
