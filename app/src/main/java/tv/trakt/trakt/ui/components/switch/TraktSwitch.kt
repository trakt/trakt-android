package tv.trakt.trakt.ui.components.switch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.theme.colors.Shade500
import tv.trakt.trakt.common.ui.theme.colors.Shade800
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun TraktSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        thumbContent = {
            Spacer(
                modifier = Modifier
                    .rotate(if (checked) 45F else -45F)
                    .background(Color.White)
                    .size(2.dp, 8.dp),
            )
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = TraktTheme.colors.switchThumbChecked,
            uncheckedThumbColor = TraktTheme.colors.switchThumbUnchecked,
            checkedTrackColor = TraktTheme.colors.switchContainerChecked,
            uncheckedTrackColor = TraktTheme.colors.switchContainerUnchecked,
            checkedBorderColor = TraktTheme.colors.switchContainerChecked,
            uncheckedBorderColor = TraktTheme.colors.switchContainerUnchecked,
            // Disabled
            disabledUncheckedTrackColor = Shade800,
            disabledUncheckedThumbColor = Shade500,
            disabledCheckedBorderColor = Shade800,
            disabledUncheckedBorderColor = Shade800,
            disabledCheckedTrackColor = Shade800,
            disabledCheckedThumbColor = Shade500,
        ),
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        TraktSwitch(
            checked = true,
            onCheckedChange = {},
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        TraktSwitch(
            checked = false,
            onCheckedChange = {},
        )
    }
}

@Preview
@Composable
private fun Preview3() {
    TraktTheme {
        TraktSwitch(
            checked = true,
            enabled = false,
            onCheckedChange = {},
        )
    }
}

@Preview
@Composable
private fun Preview4() {
    TraktTheme {
        TraktSwitch(
            checked = false,
            enabled = false,
            onCheckedChange = {},
        )
    }
}
