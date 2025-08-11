package tv.trakt.trakt.app.helpers.serializers

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class ImmutableListSerializer<T>(
    private val dataSerializer: KSerializer<T>,
) : KSerializer<ImmutableList<T>> {
    override val descriptor: SerialDescriptor = ListSerializer(dataSerializer).descriptor

    override fun serialize(
        encoder: Encoder,
        value: ImmutableList<T>,
    ) {
        return ListSerializer(dataSerializer).serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): ImmutableList<T> {
        return ListSerializer(dataSerializer).deserialize(decoder).toPersistentList()
    }
}
