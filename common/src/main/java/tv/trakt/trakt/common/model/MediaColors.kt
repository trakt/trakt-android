package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import tv.trakt.trakt.app.helpers.serializers.ColorSerializer

@Immutable
@Serializable
data class MediaColors(
    val colors: Pair<
        @Serializable(with = ColorSerializer::class)
        Color,
        @Serializable(with = ColorSerializer::class)
        Color,
    >,
)
