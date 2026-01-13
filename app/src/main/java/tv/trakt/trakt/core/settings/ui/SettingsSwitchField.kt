package tv.trakt.trakt.core.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.settings.SECTION_ITEM_HEIGHT_DP
import tv.trakt.trakt.ui.components.switch.TraktSwitch
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
fun SettingsSwitchField(
    text: String,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = { },
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(SECTION_ITEM_HEIGHT_DP.dp)
            .onClick(
                onClick = onClick,
                enabled = enabled,
            ),
    ) {
        Text(
            text = text,
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraph.copy(
                fontSize = 14.sp,
            ),
        )

        TraktSwitch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            modifier = Modifier
                .onClick(
                    onClick = onClick,
                    enabled = enabled,
                ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        SettingsSwitchField(
            text = "Notifications",
            modifier = Modifier
                .padding(16.dp),
        )
    }
}
