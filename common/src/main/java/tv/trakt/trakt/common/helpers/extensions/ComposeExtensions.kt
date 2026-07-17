@file:Suppress("ktlint:standard:filename")

package tv.trakt.trakt.common.helpers.extensions

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
    showBackground = false,
    backgroundColor = 0xFF201E23,
    locale = "us",
)
annotation class DeviceSheetPreview

fun MutableStateFlow<*>.isNotNull(): Boolean {
    return this.value != null
}

fun MutableState<*>.isNull(): Boolean {
    return this.value == null
}
