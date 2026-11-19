package tv.trakt.trakt.common.model.livingposter

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import tv.trakt.trakt.common.data.dto.LivingPosterManifestDto
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect.MaskSource

private const val SUPPORTED_VERSION = 1
private const val MAX_SHIFT = 1.5F
private val fileNamePattern = Regex("^[\\w-]+\\.webp$")
private val cutStampPattern = Regex("^[0-9a-f]{6,40}$")

@Immutable
data class LivingPoster(
    val layers: ImmutableList<Layer>,
    val mask: String?,
    val effects: ImmutableList<LivingPosterEffect>,
) {
    /** [shift] is the layer depth: negative layers move against the tilt, positive ones with it. */
    @Immutable
    data class Layer(
        val url: String,
        val shift: Float,
    )

    fun maskUrl(effect: LivingPosterEffect): String? {
        return when (effect.mask) {
            MaskSource.Letters -> mask
            MaskSource.Title -> layers.lastOrNull()?.url
            null -> null
        }
    }

    companion object {
        fun fromDto(
            dto: LivingPosterManifestDto,
            baseUrl: String,
        ): LivingPoster? {
            if (dto.v != SUPPORTED_VERSION) return null
            if (dto.mask != null && !fileNamePattern.matches(dto.mask)) return null
            if (dto.cut != null && !cutStampPattern.matches(dto.cut)) return null

            val rawLayers = dto.layers.orEmpty()
            val parsedLayers = rawLayers.mapNotNull(::toLayerFile)
            if (parsedLayers.isEmpty() || parsedLayers.size != rawLayers.size) return null

            fun toUrl(file: String): String {
                return when (dto.cut) {
                    null -> "$baseUrl/$file"
                    else -> "$baseUrl/$file?v=${dto.cut}"
                }
            }

            val hasMask = dto.mask != null
            val effects = dto.effects.orEmpty()
                .mapNotNull(LivingPosterEffect::fromDto)
                .filter {
                    when (it.mask) {
                        null -> true
                        MaskSource.Letters -> hasMask
                        MaskSource.Title -> parsedLayers.size > 1
                    }
                }

            return LivingPoster(
                layers = parsedLayers
                    .map { (file, shift) ->
                        Layer(
                            url = toUrl(file),
                            shift = shift.coerceIn(-MAX_SHIFT, MAX_SHIFT),
                        )
                    }
                    .toImmutableList(),
                mask = dto.mask?.let(::toUrl),
                effects = effects.toImmutableList(),
            )
        }

        private fun toLayerFile(tuple: JsonElement): Pair<String, Float>? {
            val values = (tuple as? JsonArray)?.takeIf { it.size == 2 } ?: return null
            val file = (values[0] as? JsonPrimitive)
                ?.takeIf { it.isString }
                ?.content
                ?.takeIf { fileNamePattern.matches(it) }
                ?: return null
            val shift = values[1].toFiniteNumber() ?: return null

            return file to shift
        }
    }
}
