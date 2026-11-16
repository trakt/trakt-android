package tv.trakt.trakt.core.summary.ui.header

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsHeaderNotesButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(R.drawable.ic_notes),
        tint = TraktTheme.colors.textPrimary,
        contentDescription = null,
        modifier = modifier
            .padding(start = 20.dp, top = 20.dp)
            .size(24.dp)
            .onClick(onClick = onClick),
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        DetailsHeaderNotesButton(onClick = {})
    }
}
