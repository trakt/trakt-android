package tv.trakt.trakt.core.summary.livingposter.ui.painters

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect.Kind
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane
import tv.trakt.trakt.core.summary.livingposter.model.SeededEffect
import tv.trakt.trakt.core.summary.livingposter.ui.LivingPosterFrame

/** Poster width in [EffectScene.unit]s, so effects keep their proportions at any size. */
internal const val UNIT_WIDTH = 300F

internal fun interface SkyLuminance {
    /** Luminance in 0..1 at poster fractions [x], [y]. */
    fun at(
        x: Float,
        y: Float,
    ): Float
}

/** Background luminance for star placement: pending until sampling finished, which can fail. */
internal sealed interface SkySample {
    data object Pending : SkySample

    class Sampled(
        val luminance: SkyLuminance?,
    ) : SkySample
}

/** Loaded layers painted effects read from. */
internal class EffectSources(
    val backdrop: Painter?,
    val backdropBitmap: Bitmap?,
    val figures: Painter?,
    val sky: SkySample,
)

internal class EffectScene(
    val frame: LivingPosterFrame,
    val unit: Float,
    val sources: EffectSources,
)

/**
 * Draws one effect onto the planes it uses. Painters keep their own simulation state between
 * frames and are not thread-safe: only [prepare] may run off the main thread, before any drawing.
 */
internal interface EffectPainter {
    /** Builds sprites, textures and warm-up state, so the first frames draw without stalls. */
    fun prepare() = Unit

    /** False when [plane] would draw nothing at [seconds], so its layer can be skipped. */
    fun hasContent(
        plane: EffectPlane,
        seconds: Float,
    ): Boolean = true

    fun DrawScope.paint(
        plane: EffectPlane,
        scene: EffectScene,
    )

    /** A colour filter for the whole poster at [seconds], or null to leave it untouched. */
    fun planeFilter(seconds: Float): ColorFilter? = null
}

internal fun createEffectPainter(seeded: SeededEffect): EffectPainter? {
    val (effect, seed) = seeded

    return when (effect.kind) {
        Kind.Embers -> EmbersPainter(effect, seed)
        Kind.Snow -> SnowPainter(effect, seed)
        Kind.Dust -> DustPainter(effect, seed)
        Kind.Rain -> RainPainter(effect, seed)
        Kind.Fireflies -> FirefliesPainter(effect, seed)
        Kind.Twinkle -> TwinklePainter(effect, seed)
        Kind.Fog -> FogPainter(effect)
        Kind.Flare -> FlarePainter(effect, seed)
        Kind.Lightning -> LightningPainter(effect, seed)
        Kind.Haze, Kind.Flicker, Kind.Glow, Kind.Bulbs, Kind.Shimmer, Kind.Foil -> null
    }
}
