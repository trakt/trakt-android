package tv.trakt.trakt.core.summary.livingposter.ui.painters

import kotlin.math.floor

private const val NOISE_POINTS = 256
private const val NOISE_MASK = NOISE_POINTS - 1

/** Deterministic, so every poster renders the same effect on every visit. */
internal class SeededRandom(
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

private fun ease(fraction: Float): Float = fraction * fraction * (3F - 2F * fraction)

/** Smooth 1D value noise in 0..1. */
internal class Noise(
    seed: Int,
) {
    private val points = SeededRandom(seed).let { random -> FloatArray(NOISE_POINTS) { random.next() } }

    fun at(position: Float): Float {
        val index = floor(position).toInt()
        val eased = ease(position - index)
        val from = points[index and NOISE_MASK]
        val to = points[(index + 1) and NOISE_MASK]

        return from * (1F - eased) + to * eased
    }
}

/** Three octaves of [Noise] in roughly -1..1. */
internal class FractalNoise(
    seed: Int,
) {
    private val coarse = Noise(seed)
    private val middle = Noise(seed + 17)
    private val fine = Noise(seed + 59)

    fun at(position: Float): Float {
        return (coarse.at(position) * 2F - 1F) * 0.6F +
            (middle.at(position * 2.3F + 7.1F) * 2F - 1F) * 0.3F +
            (fine.at(position * 5.3F + 3.7F) * 2F - 1F) * 0.1F
    }
}

/** Smooth 2D value noise in -1..1. */
internal class Noise2d(
    seed: Int,
) {
    private val order: IntArray
    private val values: FloatArray

    init {
        val random = SeededRandom(seed)
        order = (0 until NOISE_POINTS)
            .map { index -> index to random.next() }
            .sortedBy { (_, rank) -> rank }
            .map { (index, _) -> index }
            .toIntArray()
        values = FloatArray(NOISE_POINTS) { random.next() * 2F - 1F }
    }

    private fun shuffle(index: Int): Int = order[index and NOISE_MASK]

    private fun at(
        column: Int,
        row: Int,
    ): Float = values[shuffle(shuffle(column) + row)]

    fun at(
        x: Float,
        y: Float,
    ): Float {
        val column = floor(x).toInt()
        val row = floor(y).toInt()
        val u = ease(x - column)
        val v = ease(y - row)
        val topLeft = at(column, row)
        val topRight = at(column + 1, row)
        val bottomLeft = at(column, row + 1)
        val bottomRight = at(column + 1, row + 1)

        return topLeft +
            (topRight - topLeft) * u +
            (bottomLeft - topLeft + (bottomRight - bottomLeft - topRight + topLeft) * u) * v
    }
}

internal fun interpolate(
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

internal fun smoothstep(
    from: Float,
    to: Float,
    value: Float,
): Float {
    val amount = ((value - from) / (to - from)).coerceIn(0F, 1F)
    return amount * amount * (3F - 2F * amount)
}

/** Scales particle counts and strengths: 0.5 at zero intensity, 1.5 at full. */
internal fun intensityScale(intensity: Float): Float = 0.5F + intensity.coerceIn(0F, 1F)
