package tv.trakt.trakt.core.lists.sections.smart.templates.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SmartListTemplatesIntro(
    onHideClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.smart_list_templates_intro),
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1F),
        )
        GhostButton(
            text = stringResource(R.string.button_text_hide),
            iconSize = 16.dp,
            fillWidth = false,
            uppercase = false,
            horizontalPadding = 14.dp,
            corner = 64.dp,
            containerColor = Color.Transparent,
            border = BorderStroke(1.dp, TraktTheme.colors.chipContainer),
            onClick = onHideClick,
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SmartListTemplatesIntro(
            onHideClick = {},
            modifier = Modifier.padding(PaddingValues(horizontal = 16.dp)),
        )
    }
}
