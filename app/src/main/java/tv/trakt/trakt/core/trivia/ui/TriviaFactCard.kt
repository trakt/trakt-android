package tv.trakt.trakt.core.trivia.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import tv.trakt.trakt.common.helpers.extensions.toMarkdownText
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.trivia.TriviaFact
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.core.applinks.rememberAppLinkUriHandler
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun TriviaFactCard(
    fact: TriviaFact,
    modifier: Modifier = Modifier,
    spoilerVisible: Boolean = false,
    onPersonLinkClick: (SlugId) -> Unit = {},
) {
    val personLinkUriHandler = rememberAppLinkUriHandler(onPersonClick = onPersonLinkClick)

    Card(
        modifier = modifier,
        shape = DefaultCardShape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = TraktTheme.colors.shadowDynamicDefault,
            pressedElevation = TraktTheme.colors.shadowDynamicDefault,
            disabledElevation = TraktTheme.colors.shadowDynamicDefault,
        ),
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
                    color = Red400,
                    style = TraktTheme.typography.meta.copy(lineHeight = 1.4.em),
                )
            }

            val linkColor = TraktTheme.colors.textPrimary
            CompositionLocalProvider(LocalUriHandler provides personLinkUriHandler) {
                Text(
                    text = remember(fact.text, linkColor) { fact.text.toMarkdownText(linkColor) },
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.paragraphSmall.copy(lineHeight = 1.4.em),
                )
            }
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
    TraktThemeLightDark {
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
    TraktThemeLightDark {
        TriviaFactCard(
            fact = TriviaFact(
                id = "2",
                category = "plot",
                text = "This scene **reveals** the killer, [Jane Doe](https://app.trakt.tv/people/jane-doe).",
                order = 2,
                spoiler = true,
            ),
            spoilerVisible = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}
