package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class MediaColors(
    val colors: Pair<Color, Color>,
)
