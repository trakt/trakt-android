package tv.trakt.trakt.app.common.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal data class StreamingService(
    val source: String,
    val name: String,
    val logo: String?,
    val linkDirect: String?,
    val uhd: Boolean,
    val color: Color?,
)
