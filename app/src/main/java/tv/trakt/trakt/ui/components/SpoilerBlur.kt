package tv.trakt.trakt.ui.components

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.ui.composables.LocalSpoilerReveal
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.ui.theme.TraktTheme

private val SpoilerBlurRadius = 6.dp

internal fun Modifier.spoilerBlur(
    radius: Dp = SpoilerBlurRadius,
    edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Unbounded,
): Modifier = blur(radius, edgeTreatment)

@Composable
internal fun SpoilerBlur(
    hidden: Boolean,
    onReveal: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var revealed by rememberSaveable { mutableStateOf(false) }
    val isHidden = hidden && !revealed

    Box(
        modifier = modifier.then(
            if (isHidden) Modifier.spoilerBlur() else Modifier,
        ),
    ) {
        content()

        // Sits above the content so its own click handlers cannot swallow the reveal tap.
        if (isHidden) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .onClick {
                        revealed = true
                        onReveal()
                    },
            )
        }
    }
}

@Composable
internal fun SpoilerBlur(
    hidden: Boolean,
    type: MediaType,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val onReveal = LocalSpoilerReveal.current

    SpoilerBlur(
        hidden = hidden,
        onReveal = { onReveal(type) },
        modifier = modifier,
        content = content,
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Column(verticalArrangement = spacedBy(8.dp)) {
            SpoilerBlur(hidden = true, onReveal = {}) {
                Text(
                    text = "62 episodes",
                    style = TraktTheme.typography.cardSubtitle,
                    color = TraktTheme.colors.textPrimary,
                )
            }
            SpoilerBlur(hidden = false, onReveal = {}) {
                Text(
                    text = "62 episodes",
                    style = TraktTheme.typography.cardSubtitle,
                    color = TraktTheme.colors.textPrimary,
                )
            }
        }
    }
}
