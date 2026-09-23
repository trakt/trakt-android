package tv.trakt.trakt.core.klipy.ui

import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import coil3.compose.rememberAsyncImagePainter

/**
 * Painter for a KLIPY `blur_preview` - a tiny JPEG shipped inline as a base64 data URI, shown
 * under the GIF while it downloads. Null when the preview is missing or malformed.
 */
@Composable
internal fun rememberBlurPreviewPainter(blurPreview: String?): Painter? {
    val bytes = remember(blurPreview) { blurPreview?.decodeDataUri() }
    return when (bytes) {
        null -> null
        else -> rememberAsyncImagePainter(model = bytes)
    }
}

private fun String.decodeDataUri(): ByteArray? {
    val payload = substringAfter(',', missingDelimiterValue = this)
    if (payload.isBlank()) return null

    return runCatching { Base64.decode(payload, Base64.DEFAULT) }
        .getOrNull()
        ?.takeIf { it.isNotEmpty() }
}
