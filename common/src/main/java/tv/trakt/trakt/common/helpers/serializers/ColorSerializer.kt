package tv.trakt.trakt.common.helpers.serializers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Kotlin serializer for [Color] that serializes it as an integer.
 * The integer is expected to be in ARGB format.
 */
object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "Color",
        PrimitiveKind.INT,
    )

    override fun serialize(
        encoder: Encoder,
        value: Color,
    ) {
        encoder.encodeInt(value.toArgb())
    }

    override fun deserialize(decoder: Decoder): Color {
        val colorInt = decoder.decodeInt()
        return try {
            Color(colorInt)
        } catch (error: Exception) {
            throw SerializationException("Failed to parse Color from: $colorInt", error)
        }
    }
}
