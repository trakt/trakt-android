package tv.trakt.trakt.tv.core.search

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import tv.trakt.trakt.tv.ui.theme.TraktTheme

@Composable
internal fun SearchScreen(modifier: Modifier = Modifier) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Placeholder(
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusable(enabled = true),
        )
    }
}

@Composable
fun Placeholder(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Search Screen",
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading4,
            modifier = modifier,
        )
        Text(
            text = "\"I am looking for something.\nI don’t know what it is, but I know I have to find it.\"",
            color = TraktTheme.colors.textPrimary.copy(alpha = 0.5F),
            style = TraktTheme.typography.heading5.copy(fontSize = 20.sp),
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            text = "- The Secret Life of Walter Mitty (2013)",
            color = TraktTheme.colors.textPrimary.copy(alpha = 0.5F),
            style = TraktTheme.typography.heading5.copy(fontSize = 14.sp),
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun MainScreenPreview() {
    TraktTheme {
        SearchScreen()
    }
}
