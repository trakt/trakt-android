package tv.trakt.trakt.core.trivia.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import tv.trakt.trakt.common.helpers.extensions.toAnnotatedString
import tv.trakt.trakt.common.model.trivia.TriviaFact
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun TriviaFactCard(
    fact: TriviaFact,
    modifier: Modifier = Modifier,
    spoilerVisible: Boolean = false,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = cardColors(
            containerColor = TraktTheme.colors.commentContainer,
        ),
    ) {
        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            if (spoilerVisible && fact.spoiler) {
                Text(
                    text = stringResource(R.string.text_spoiler),
                    color = Red500,
                    style = TraktTheme.typography.paragraphSmall.copy(lineHeight = 1.4.em),
                )
            }

            Text(
                text = remember(fact.text) {
                    HtmlCompat
                        .fromHtml(fact.text, FROM_HTML_MODE_LEGACY)
                        .toAnnotatedString()
                },
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmall.copy(lineHeight = 1.4.em),
            )
        }
    }
}

// -- Previews --

@Preview(
    device = "id:pixel_7",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewRegular() {
    TraktTheme {
        TriviaFactCard(
            fact = TriviaFact(
                id = "1",
                category = "production",
                text = "The famous chase scene was filmed in a single take over three days.",
                order = 1,
                spoiler = false,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    device = "id:pixel_7",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewSpoiler() {
    TraktTheme {
        TriviaFactCard(
            fact = TriviaFact(
                id = "2",
                category = "plot",
                text = "This scene <b>reveals</b> the true identity of the killer.",
                order = 2,
                spoiler = true,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
