package tv.trakt.trakt.common.helpers.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

/**
 * Kotlin serializer for [Instant] that handles epoch milliseconds serialization/deserialization.
 * Uses epoch milliseconds (long) for both serialization and deserialization.
 */
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "Instant",
        PrimitiveKind.LONG,
    )

    override fun serialize(
        encoder: Encoder,
        value: Instant,
    ) {
        encoder.encodeLong(value.toEpochMilli())
    }

    override fun deserialize(decoder: Decoder): Instant {
        val dateMillis = decoder.decodeLong()
        return try {
            Instant.ofEpochMilli(dateMillis)
        } catch (e: Exception) {
            throw SerializationException("Failed to parse Instant from: $dateMillis", e)
        }
    }
}
