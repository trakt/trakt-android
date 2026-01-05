package tv.trakt.trakt.core.welcome.onboarding.pages

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun OnboardingPage4(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = spacedBy(32.dp, alignment = Alignment.Bottom),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_discover_off),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(42.dp),
        )
        Text(
            text = stringResource(R.string.text_onboarding_4_title),
            style = TraktTheme.typography.heading3.copy(
                lineHeight = 1.4.em,
            ),
            color = Color.White,
            textAlign = TextAlign.Start,
        )

        Text(
            text = stringResource(R.string.text_onboarding_4_description),
            style = TraktTheme.typography.paragraphSmaller.copy(
                lineHeight = 1.4.em,
            ),
            color = Color.White,
            textAlign = TextAlign.Start,
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
        OnboardingPage4()
    }
}
