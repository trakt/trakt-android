package tv.trakt.trakt.core.summary.livingposter.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect.Kind
import kotlin.math.roundToInt

private const val STREAK_ROTATION = 12F

/** Positions are percentages of the particle's effect box. */
@Immutable
internal data class LivingPosterParticle(
    val kind: Kind,
    val box: LivingPosterEffect.Box,
    val x: Float,
    val y: Float,
    val size: Float,
    val length: Float,
    val dx: Float,
    val duration: Float,
    val phase: Float,
    val color: String,
) {
    @Immutable
    data class Frame(
        val x: Float,
        val y: Float,
        val opacity: Float,
        val scale: Float,
        val rotation: Float,
    )

    fun frame(seconds: Float): Frame {
        val progress = (seconds / duration + phase) % 1F

        return when (kind) {
            Kind.Embers -> Frame(
                x = x + dx * progress,
                y = y - 110F * progress,
                opacity = interpolate(progress, 0F to 0F, 0.12F to 1F, 1F to 0F),
                scale = 1F - 0.6F * progress,
                rotation = 0F,
            )

            Kind.Snow -> Frame(
                x = x + dx * progress,
                y = y + interpolate(progress, 0F to -10F, 1F to 115F),
                opacity = interpolate(progress, 0F to 0F, 0.1F to 0.95F, 1F to 0.2F),
                scale = 1F,
                rotation = 0F,
            )

            Kind.Dust -> Frame(
                x = x + interpolate(progress, 0F to 0F, 0.5F to dx, 1F to 0F),
                y = y + interpolate(progress, 0F to 0F, 0.5F to -6F, 1F to 0F),
                opacity = interpolate(progress, 0F to 0.15F, 0.5F to 0.85F, 1F to 0.15F),
                scale = 1F,
                rotation = 0F,
            )

            Kind.Rain -> Frame(
                x = x - 12F * progress,
                y = y + interpolate(progress, 0F to -20F, 1F to 120F),
                opacity = interpolate(progress, 0F to 0F, 0.15F to 0.7F, 1F to 0F),
                scale = 1F,
                rotation = STREAK_ROTATION,
            )

            else -> Frame(
                x = x,
                y = y,
                opacity = interpolate(progress, 0F to 0F, 0.45F to 1F, 0.55F to 1F, 1F to 0F),
                scale = interpolate(progress, 0F to 0.6F, 0.45F to 1F, 0.55F to 1F, 1F to 0.6F),
                rotation = 0F,
            )
        }
    }

    companion object {
        private val counts = mapOf(
            Kind.Embers to 40,
            Kind.Snow to 46,
            Kind.Dust to 28,
            Kind.Rain to 40,
            Kind.Fireflies to 14,
            Kind.Twinkle to 18,
        )

        private val colors = mapOf(
            Kind.Embers to "#ffa54a",
            Kind.Snow to "#ffffff",
            Kind.Dust to "#f2e7d2",
            Kind.Rain to "#cfe0ff",
            Kind.Fireflies to "#e8ff8a",
            Kind.Twinkle to "#ffffff",
        )

        fun create(
            effect: LivingPosterEffect,
            seed: Int,
        ): List<LivingPosterParticle> {
            val kind = effect.kind
            val baseCount = counts[kind] ?: return emptyList()
            val random = SeededRandom(seed)
            val count = (baseCount * (0.5F + effect.intensity)).roundToInt()

            return List(count) {
                val x = random.next() * 100F
                val size = when (kind) {
                    Kind.Embers -> 1F + random.next() * 1.6F
                    Kind.Snow -> 2F + random.next() * 4F
                    Kind.Dust -> 1F + random.next() * 2.2F
                    Kind.Fireflies -> 3F + random.next() * 3F
                    Kind.Twinkle -> 2F + random.next() * 2F
                    else -> 1F
                }
                val y = when (kind) {
                    Kind.Embers -> 70F + random.next() * 40F
                    Kind.Snow, Kind.Rain -> -10F
                    else -> random.next() * 100F
                }
                val length = if (kind == Kind.Rain) 14F + random.next() * 18F else size
                val dx = (random.next() - 0.5F) * 40F
                val duration = when (kind) {
                    Kind.Embers -> 4F + random.next() * 5F
                    Kind.Snow -> 7F + random.next() * 7F
                    Kind.Dust -> 6F + random.next() * 8F
                    Kind.Rain -> 0.7F + random.next() * 0.5F
                    Kind.Fireflies -> 2.5F + random.next() * 3F
                    else -> 2F + random.next() * 3F
                }
                val phase = random.next()

                LivingPosterParticle(
                    kind = kind,
                    box = effect.box,
                    x = x,
                    y = y,
                    size = size,
                    length = length,
                    dx = dx,
                    duration = duration,
                    phase = phase,
                    color = effect.color ?: colors.getValue(kind),
                )
            }
        }
    }
}

private fun interpolate(
    progress: Float,
    vararg keyframes: Pair<Float, Float>,
): Float {
    val next = keyframes.indexOfFirst { (at, _) -> at >= progress }
    if (next <= 0) return keyframes.first().second

    val (fromAt, fromValue) = keyframes[next - 1]
    val (toAt, toValue) = keyframes[next]
    val span = toAt - fromAt
    if (span == 0F) return toValue

    return fromValue + (toValue - fromValue) * ((progress - fromAt) / span)
}

/** Deterministic, so every poster renders the same particle field on every visit. */
private class SeededRandom(
    seed: Int,
) {
    private var state = seed

    fun next(): Float {
        state += 0x6d2b79f5
        val mixed = (state xor (state ushr 15)) * (state or 1)
        val scrambled = mixed xor (mixed + (mixed xor (mixed ushr 7)) * (mixed or 61))
        val value = (scrambled xor (scrambled ushr 14)).toLong() and 0xFFFFFFFFL

        return (value / 4294967296.0).toFloat()
    }
}
