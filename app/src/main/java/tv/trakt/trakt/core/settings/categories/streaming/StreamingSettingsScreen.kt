package tv.trakt.trakt.core.settings.categories.streaming

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.core.settings.ui.SettingsSegmentedGroup
import tv.trakt.trakt.core.settings.ui.SettingsSubscreen
import tv.trakt.trakt.core.settings.ui.SettingsTextField
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R

@Composable
internal fun StreamingSettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateYounify: () -> Unit = { },
    onNavigateBack: () -> Unit = { },
) {
    SettingsSubscreen(
        title = stringResource(R.string.link_text_streaming_sync_settings),
        onBackClick = onNavigateBack,
        modifier = modifier,
    ) {
        SettingsSegmentedGroup {
            item {
                SettingsTextField(
                    text = stringResource(R.string.text_automatic_tracking),
                    onClick = onNavigateYounify,
                )
            }
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        StreamingSettingsScreen()
    }
}
