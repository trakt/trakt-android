package tv.trakt.trakt.widgets.configuration

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.core.settings.ui.SettingsSwitchField
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.widgets.model.WidgetBackground

@Composable
internal fun WidgetConfigurationView(
    state: WidgetConfigurationState,
    modifier: Modifier = Modifier,
    onBackgroundClick: (WidgetBackground) -> Unit = { },
    onTitleVisibleClick: (Boolean) -> Unit = { },
    onDoneClick: () -> Unit = { },
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier.fillMaxWidth(),
    ) {
        TraktHeader(
            title = stringResource(R.string.page_title_widget_background),
            subtitle = stringResource(R.string.text_info_widget_background),
            maxSubtitleLines = 2,
        )

        Column(
            verticalArrangement = spacedBy(TraktTheme.spacing.chipsSpace),
            modifier = Modifier
                .graphicsLayer {
                    translationX = -8.dp.toPx()
                },
        ) {
            for (background in WidgetBackground.entries) {
                GhostButton(
                    text = stringResource(background.displayStringRes),
                    contentColor = when (background) {
                        state.background -> TraktTheme.colors.primaryButtonContent
                        else -> TraktTheme.colors.textSecondary
                    },
                    icon = when (background) {
                        state.background -> painterResource(R.drawable.ic_check_google)
                        else -> null
                    },
                    iconSpace = 8.dp,
                    onClick = { onBackgroundClick(background) },
                )
            }
        }

        SettingsSwitchField(
            text = stringResource(R.string.text_settings_widget_show_title),
            description = stringResource(R.string.text_settings_widget_show_title_description),
            checked = state.titleVisible,
            onClick = { onTitleVisibleClick(!state.titleVisible) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 12.dp),
        )

        PrimaryButton(
            text = stringResource(R.string.button_text_apply),
            onClick = onDoneClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = TraktTheme.spacing.chipsSpace),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TraktTheme {
        WidgetConfigurationView(
            state = WidgetConfigurationState(
                background = WidgetBackground.SemiTransparent,
            ),
        )
    }
}
