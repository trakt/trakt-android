package tv.trakt.trakt.common.core.klipy.model

import androidx.compose.runtime.Immutable

@Immutable
data class GifCategory(
    val title: String,
    val query: String,
    val previewUrl: String,
)
