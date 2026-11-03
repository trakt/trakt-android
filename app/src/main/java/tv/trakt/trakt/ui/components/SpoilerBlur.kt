package tv.trakt.trakt.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

internal val LocalSpoilersHidden = compositionLocalOf { false }

@Composable
internal fun SpoilerBlur(
    modifier: Modifier = Modifier,
    hidden: Boolean = LocalSpoilersHidden.current,
    revealed: Boolean = false,
    content: @Composable () -> Unit,
) {
    var isRevealed by rememberSaveable { mutableStateOf(false) }
    val revealLabel = stringResource(R.string.button_label_reveal_spoiler)
    val blurred = hidden && !revealed && !isRevealed

    Box(
        modifier = modifier.then(
            if (blurred) {
                Modifier
                    .blur(4.dp)
                    .semantics { contentDescription = revealLabel }
                    .onClick { isRevealed = true }
            } else {
                Modifier
            },
        ),
    ) {
        content()
    }
}

@Preview
@Composable
private fun Preview() {
    TraktThemeLightDark {
        Column {
            SpoilerBlur(hidden = true) {
                Text("62 episodes", color = TraktTheme.colors.textPrimary)
            }
            SpoilerBlur(hidden = true, revealed = true) {
                Text("62 episodes", color = TraktTheme.colors.textPrimary)
            }
        }
    }
}
