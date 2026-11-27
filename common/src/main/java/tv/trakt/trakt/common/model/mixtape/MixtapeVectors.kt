package tv.trakt.trakt.common.model.mixtape

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.data.dto.MixtapeVectorsDto
import tv.trakt.trakt.common.model.TraktId
import kotlin.io.encoding.Base64

data class MixtapeVectors(
    val cutoff: Double = 1.0,
    val vectors: ImmutableMap<TraktId, ByteArray> = persistentMapOf(),
) {
    companion object {
        fun fromDto(dto: MixtapeVectorsDto): MixtapeVectors {
            return MixtapeVectors(
                cutoff = dto.cutoff,
                vectors = dto.vectors
                    .mapNotNull { (id, encoded) ->
                        val traktId = id.toIntOrNull()?.let(::TraktId) ?: return@mapNotNull null
                        val vector = runCatching { Base64.decode(encoded) }.getOrNull() ?: return@mapNotNull null
                        traktId to vector
                    }
                    .toMap()
                    .toImmutableMap(),
            )
        }
    }
}
