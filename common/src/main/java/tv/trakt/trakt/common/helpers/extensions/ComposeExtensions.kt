@file:Suppress("ktlint:standard:filename")

package tv.trakt.trakt.common.helpers.extensions

import android.content.res.Configuration
import androidx.compose.runtime.MutableState
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow

@Preview(
    device = "id:pixel_7",
    showBackground = true,
    backgroundColor = 0xFF151418,
    locale = "us",
)
annotation class DevicePreview

@Preview(
    device = "id:pixel_7",
    showBackground = true,
    backgroundColor = 0xFF151418,
    locale = "ar",
)
annotation class DevicePreviewRtl

@Preview(
    device = "id:pixel_7",
    showBackground = false,
    backgroundColor = 0xFF201E23,
    locale = "us",
)
annotation class DeviceSheetPreview

@Preview(
    showBackground = true,
    backgroundColor = 0xFF151418,
)
annotation class PreviewTraktDark

@Preview(
    showBackground = true,
    backgroundColor = 0xFFF1EFF4,
)
annotation class PreviewTraktLight

@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF151418,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    name = "Light",
    showBackground = true,
    backgroundColor = 0xFFF1EFF4,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
annotation class PreviewTraktLightDark

fun MutableStateFlow<*>.isNotNull(): Boolean {
    return this.value != null
}

fun MutableState<*>.isNull(): Boolean {
    return this.value == null
}
