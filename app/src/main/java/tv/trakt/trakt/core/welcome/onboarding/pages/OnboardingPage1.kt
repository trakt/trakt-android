package tv.trakt.trakt.core.welcome.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun OnboardingPage1(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Onboarding Page 1",
            style = TraktTheme.typography.heading4,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(
    device = "id:pixel_4",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        OnboardingPage1(
            modifier = Modifier.fillMaxSize(),
        )
    }
}
