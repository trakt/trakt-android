@file:Suppress("ktlint:standard:filename")

package tv.trakt.trakt.common.helpers.extensions

import androidx.compose.runtime.MutableState
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow

@Preview(
    device = "id:pixel_7",
    showBackground = false,
    backgroundColor = 0xFFFFFF,
    locale = "us",
)
annotation class DevicePreview

fun MutableStateFlow<*>.isNotNull(): Boolean {
    return this.value != null
}

fun MutableState<*>.isNull(): Boolean {
    return this.value == null
}
