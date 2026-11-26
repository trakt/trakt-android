package tv.trakt.trakt.core.summary.livingposter.ui.painters

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

/** Loaded layers painted effects read from. [luminance] stays null until the backdrop is sampled. */
internal class EffectSources(
    val backdrop: Painter?,
    val figures: Painter?,
    val luminance: SkyLuminance?,
)

internal class EffectScene(
    val frame: LivingPosterFrame,
    val unit: Float,
    val sources: EffectSources,
)

/** Draws one effect onto the planes it uses. Painters keep their own simulation state between frames. */
internal interface EffectPainter {
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
