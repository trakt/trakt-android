package tv.trakt.trakt.core.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
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
    description: String? = null,
    checked: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = { },
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(SECTION_ITEM_HEIGHT_DP.dp),
    ) {
        Column(
            verticalArrangement = spacedBy(3.dp),
            modifier = Modifier.weight(1f, fill = false),
        ) {
            Text(
                text = text,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraph.copy(
                    fontSize = 14.sp,
                ),
            )

            description?.let {
                Text(
                    text = it,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraph.copy(
                        fontSize = 12.sp,
                    ),
                    maxLines = 3,
                    overflow = Ellipsis,
                )
            }
        }

        TraktSwitch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            modifier = Modifier
                .padding(start = 16.dp)
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

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        SettingsSwitchField(
            text = "Notifications",
            description = "Allow more than 1 play for movies and episodes.",
            modifier = Modifier
                .padding(16.dp),
        )
    }
}
