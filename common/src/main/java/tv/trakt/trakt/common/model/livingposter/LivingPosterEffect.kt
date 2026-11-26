package tv.trakt.trakt.common.model.livingposter

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

private const val MAX_POINTS = 64
private val colorPattern = Regex("^#(?:[0-9a-f]{3,4}|[0-9a-f]{6}|[0-9a-f]{8})$", RegexOption.IGNORE_CASE)

/** Positions and sizes are percentages of the poster (or of the effect box for [Point]s). */
@Immutable
data class LivingPosterEffect(
    val kind: Kind,
    val color: String?,
    val intensity: Float,
    val box: Box,
    val points: ImmutableList<Point> = persistentListOf(),
    val at: Point? = null,
    val mask: MaskSource? = null,
) {
    enum class Kind(
        val value: String,
        val hasColor: Boolean = false,
        val hasBox: Boolean = false,
    ) {
        Embers("embers", hasColor = true, hasBox = true),
        Rain("rain"),
        Lightning("lightning"),
        Twinkle("twinkle", hasBox = true),
        Fog("fog", hasColor = true, hasBox = true),
        Haze("haze", hasColor = true),
        Snow("snow"),
        Fireflies("fireflies"),
        Dust("dust"),
        Flicker("flicker", hasColor = true, hasBox = true),
        Glow("glow", hasColor = true, hasBox = true),
        Bulbs("bulbs", hasColor = true),
        Flare("flare"),
        Shimmer("shimmer"),
        Foil("foil"),
    }

    enum class MaskSource(
        val value: String,
    ) {
        Letters("letters"),
        Title("title"),
    }

    @Immutable
    data class Box(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
    ) {
        companion object {
            val Full = Box(0F, 0F, 100F, 100F)
        }
    }

    @Immutable
    data class Point(
        val x: Float,
        val y: Float,
    )

    val isMasked: Boolean
        get() = kind == Kind.Shimmer || kind == Kind.Foil

    companion object {
        /** Parses one `[kind, color, intensity, box, extra]` manifest tuple. Unknown kinds give null. */
        fun fromDto(tuple: JsonElement): LivingPosterEffect? {
            if (tuple !is JsonArray) return null

            val kindValue = (tuple.getOrNull(0) as? JsonPrimitive)
                ?.takeIf { it.isString }
                ?.content
            val kind = Kind.entries.firstOrNull { it.value == kindValue } ?: return null
            val intensity = tuple.getOrNull(2).toFiniteNumber() ?: return null
            val extra = tuple.getOrNull(4)

            val effect = LivingPosterEffect(
                kind = kind,
                color = if (kind.hasColor) tuple.getOrNull(1).toColor() else null,
                intensity = intensity.coerceIn(0F, 1F),
                box = if (kind.hasBox) tuple.getOrNull(3).toBox() else Box.Full,
            )

            return when (kind) {
                Kind.Bulbs -> {
                    val points = extra.toPoints()
                    if (points.isEmpty()) null else effect.copy(points = points)
                }

                Kind.Flare -> {
                    val at = extra.toPoint() ?: return null
                    effect.copy(at = at)
                }

                Kind.Shimmer, Kind.Foil -> {
                    val value = (extra as? JsonPrimitive)?.takeIf { it.isString }?.content
                    val mask = MaskSource.entries.firstOrNull { it.value == value } ?: return null
                    effect.copy(mask = mask)
                }

                Kind.Embers, Kind.Rain, Kind.Lightning, Kind.Twinkle, Kind.Fog, Kind.Haze,
                Kind.Snow, Kind.Fireflies, Kind.Dust, Kind.Flicker, Kind.Glow,
                -> {
                    effect
                }
            }
        }
    }
}

internal fun JsonElement?.toFiniteNumber(): Float? {
    val primitive = this as? JsonPrimitive ?: return null
    if (primitive.isString) return null

    return primitive.doubleOrNull
        ?.takeIf { it.isFinite() }
        ?.toFloat()
}

private fun JsonElement?.toColor(): String? {
    val primitive = this as? JsonPrimitive ?: return null
    if (!primitive.isString) return null

    return primitive.content.takeIf { colorPattern.matches(it) }
}

private fun JsonElement?.toBox(): LivingPosterEffect.Box {
    val values = (this as? JsonArray)
        ?.takeIf { it.size == 4 }
        ?.map { it.toFiniteNumber() }
        ?: return LivingPosterEffect.Box.Full

    val (x, y, width, height) = values
    if (x == null || y == null || width == null || height == null) {
        return LivingPosterEffect.Box.Full
    }

    return LivingPosterEffect.Box(x, y, width, height)
}

private fun JsonElement?.toPoint(): LivingPosterEffect.Point? {
    val values = (this as? JsonArray)?.takeIf { it.size == 2 } ?: return null
    val x = values[0].toFiniteNumber() ?: return null
    val y = values[1].toFiniteNumber() ?: return null

    return LivingPosterEffect.Point(x, y)
}

private fun JsonElement?.toPoints(): ImmutableList<LivingPosterEffect.Point> {
    val values = this as? JsonArray ?: return persistentListOf()

    return values
        .take(MAX_POINTS)
        .mapNotNull { it.toPoint() }
        .toImmutableList()
}
