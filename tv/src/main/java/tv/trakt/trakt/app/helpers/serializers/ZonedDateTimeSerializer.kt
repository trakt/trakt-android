package tv.trakt.trakt.app.helpers.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import tv.trakt.trakt.app.helpers.extensions.toZonedDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Kotlin serializer for [ZonedDateTime] that handles multiple date formats.
 * Uses ISO_INSTANT format for serialization and supports parsing both ISO format
 * and Nitro endpoint format during deserialization.
 */
object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "ZonedDateTime",
        PrimitiveKind.STRING,
    )

    override fun serialize(
        encoder: Encoder,
        value: ZonedDateTime,
    ) {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_INSTANT))
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        val dateString = decoder.decodeString()
        return try {
            dateString.toZonedDateTime()
        } catch (e: Exception) {
            throw SerializationException("Failed to parse ZonedDateTime from: $dateString", e)
        }
    }
}
