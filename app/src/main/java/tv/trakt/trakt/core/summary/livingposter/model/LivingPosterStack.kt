package tv.trakt.trakt.core.summary.livingposter.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.livingposter.LivingPoster
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect.Kind
import tv.trakt.trakt.core.summary.livingposter.model.EffectBlend.Normal
import tv.trakt.trakt.core.summary.livingposter.model.EffectBlend.Screen
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane.Back
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane.Backdrop
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane.Front
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane.Rim

private const val BEHIND_SHIFT = -0.15F
private const val FLARE_SHIFT = -0.6F
private const val FRONT_SHIFT = 1F
private const val PAINTED_BASE_SEED = 42
private const val PAINTED_SEED_STEP = 101

/** Where a painted effect draws: over the background, behind the figures, on their edges or in front of them. */
internal enum class EffectPlane {
    Backdrop,
    Back,
    Rim,
    Front,
}

internal enum class EffectBlend {
    Normal,
    Screen,
}

@Immutable
internal data class SeededEffect(
    val effect: LivingPosterEffect,
    val seed: Int,
)

/** Stack keys of the layers that painted effects read: the background and the figures cutout. */
@Immutable
internal data class EffectSourceKeys(
    val backdrop: String?,
    val figures: String?,
)

@Immutable
internal sealed interface LivingPosterStackItem {
    val key: String
    val shift: Float

    @Immutable
    data class Layer(
        override val key: String,
        override val shift: Float,
        val url: String,
        val isCutout: Boolean,
    ) : LivingPosterStackItem

    @Immutable
    data class Effect(
        override val key: String,
        override val shift: Float,
        val effect: LivingPosterEffect,
        val maskUrl: String?,
    ) : LivingPosterStackItem

    @Immutable
    data class Painted(
        override val key: String,
        override val shift: Float,
        val effects: ImmutableList<SeededEffect>,
        val plane: EffectPlane,
        val sources: EffectSourceKeys,
        val isScreen: Boolean,
    ) : LivingPosterStackItem
}

/** Layer roles come from the depth shifts the poster cutter writes for each layer. */
internal enum class LivingPosterLayerRole(
    val shift: Float,
) {
    Background(-1F),
    Mid(-0.35F),
    Figures(0.55F),
    Title(1F),
    ;

    companion object {
        fun fromShift(shift: Float): LivingPosterLayerRole? {
            return entries.firstOrNull { it.shift == shift }
        }
    }
}

private val paintedPlanes: Map<Kind, Map<EffectPlane, EffectBlend>> = mapOf(
    Kind.Embers to mapOf(Backdrop to Normal, Back to Screen, Front to Screen),
    Kind.Snow to mapOf(Back to Normal, Front to Normal),
    Kind.Dust to mapOf(Back to Screen, Front to Screen),
    Kind.Rain to mapOf(Back to Normal, Front to Screen),
    Kind.Fireflies to mapOf(Back to Screen),
    Kind.Twinkle to mapOf(Back to Screen),
    Kind.Fog to mapOf(Back to Screen, Front to Screen),
    Kind.Flare to mapOf(Front to Screen),
    Kind.Lightning to mapOf(Back to Screen, Rim to Screen, Front to Screen),
)

private val LivingPosterEffect.isPainted: Boolean
    get() = kind in paintedPlanes

private val LivingPosterEffect.isAmbientBehind: Boolean
    get() = kind == Kind.Haze || kind == Kind.Flicker || kind == Kind.Glow

private val LivingPosterEffect.isOnBackground: Boolean
    get() = kind == Kind.Bulbs || (isMasked && mask == LivingPosterEffect.MaskSource.Letters)

private val LivingPosterEffect.isTitleMasked: Boolean
    get() = isMasked && mask == LivingPosterEffect.MaskSource.Title

private fun isBelowTitle(shift: Float): Boolean {
    return when (LivingPosterLayerRole.fromShift(shift)) {
        LivingPosterLayerRole.Background,
        LivingPosterLayerRole.Mid,
        LivingPosterLayerRole.Figures,
        -> true

        LivingPosterLayerRole.Title, null -> false
    }
}

private fun isCutoutLayer(
    shift: Float,
    index: Int,
    total: Int,
): Boolean {
    return when (LivingPosterLayerRole.fromShift(shift)) {
        null -> index == total - 1 && index > 0
        LivingPosterLayerRole.Figures, LivingPosterLayerRole.Title -> true
        LivingPosterLayerRole.Background, LivingPosterLayerRole.Mid -> false
    }
}

private fun layerKey(index: Int): String = "layer-$index"

private fun LivingPoster.toEffectItem(
    effect: LivingPosterEffect,
    key: String,
    shift: Float,
): LivingPosterStackItem {
    return LivingPosterStackItem.Effect(
        key = key,
        shift = shift,
        effect = effect,
        maskUrl = maskUrl(effect),
    )
}

private data class PaintedGroup(
    val key: String,
    val plane: EffectPlane,
    val blend: EffectBlend,
    val shift: Float,
)

private fun List<SeededEffect>.toPaintedItems(
    group: PaintedGroup,
    sources: EffectSourceKeys,
): List<LivingPosterStackItem> {
    val grouped = filter { paintedPlanes[it.effect.kind]?.get(group.plane) == group.blend }
    if (grouped.isEmpty()) return emptyList()

    return listOf(
        LivingPosterStackItem.Painted(
            key = group.key,
            shift = group.shift,
            effects = grouped.toImmutableList(),
            plane = group.plane,
            sources = sources,
            isScreen = group.blend == Screen,
        ),
    )
}

/**
 * Orders layers and effects back to front. Painted effects split across planes, so near
 * particles, mist and flashes can pass in front of the figures while the rest stays behind.
 */
internal fun LivingPoster.toStack(): ImmutableList<LivingPosterStackItem> {
    val painted = effects
        .filter { it.isPainted }
        .mapIndexed { index, effect ->
            SeededEffect(effect = effect, seed = PAINTED_BASE_SEED + index * PAINTED_SEED_STEP)
        }
    val (flares, unshifted) = painted.partition { it.effect.kind == Kind.Flare }
    val ambient = effects.filter { it.isAmbientBehind }
    val onBackground = effects.filter { it.isOnBackground }
    val front = effects.filter { !it.isPainted && !it.isAmbientBehind && !it.isOnBackground }

    val figuresIndex = layers.indexOfFirst {
        LivingPosterLayerRole.fromShift(it.shift) == LivingPosterLayerRole.Figures
    }
    val figures = layers.getOrNull(figuresIndex)
    val sources = EffectSourceKeys(
        backdrop = if (layers.isEmpty()) null else layerKey(0),
        figures = figures?.let { layerKey(figuresIndex) },
    )
    val backgroundShift = layers.firstOrNull()?.shift ?: 0F

    val ambientItems = ambient.mapIndexed { index, effect ->
        toEffectItem(effect, key = "behind-${effect.kind.value}-$index", shift = BEHIND_SHIFT)
    }
    val behind = ambientItems +
        painted.toPaintedItems(PaintedGroup("painted-back-screen", Back, Screen, BEHIND_SHIFT), sources) +
        painted.toPaintedItems(PaintedGroup("painted-back-normal", Back, Normal, BEHIND_SHIFT), sources)

    val paintedFront =
        unshifted.toPaintedItems(PaintedGroup("painted-front-normal", Front, Normal, FRONT_SHIFT), sources) +
            unshifted.toPaintedItems(PaintedGroup("painted-front-screen", Front, Screen, FRONT_SHIFT), sources) +
            flares.toPaintedItems(PaintedGroup("painted-front-flare", Front, Screen, FLARE_SHIFT), sources)

    val backdrop = painted.toPaintedItems(
        PaintedGroup("painted-backdrop-normal", Backdrop, Normal, backgroundShift),
        sources,
    )
    val rim = figures
        ?.let { painted.toPaintedItems(PaintedGroup("painted-rim-screen", Rim, Screen, it.shift), sources) }
        .orEmpty()

    val titleShift = layers
        .withIndex()
        .lastOrNull { (index, layer) -> index > 0 && !isBelowTitle(layer.shift) }
        ?.value
        ?.shift
        ?: FRONT_SHIFT

    fun frontShift(effect: LivingPosterEffect): Float {
        return if (effect.isTitleMasked) titleShift else FRONT_SHIFT
    }

    val behindIndex = when {
        figuresIndex >= 0 -> figuresIndex
        layers.size > 1 -> layers.size - 1
        else -> layers.size
    }

    val stack = layers.flatMapIndexed { index, layer ->
        val layerItem = LivingPosterStackItem.Layer(
            key = layerKey(index),
            shift = layer.shift,
            url = layer.url,
            isCutout = isCutoutLayer(layer.shift, index, layers.size),
        )
        val backgroundEffects = when (index) {
            0 -> backdrop + onBackground.mapIndexed { position, effect ->
                toEffectItem(effect, key = "background-${effect.kind.value}-$position", shift = backgroundShift)
            }

            else -> emptyList()
        }

        (if (index == behindIndex) behind else emptyList()) +
            layerItem +
            (if (index == figuresIndex) rim else emptyList()) +
            backgroundEffects
    }

    val trailing = if (behindIndex == layers.size) behind else emptyList()
    val frontItems = front.mapIndexed { index, effect ->
        toEffectItem(effect, key = "front-${effect.kind.value}-$index", shift = frontShift(effect))
    }

    return (stack + trailing + frontItems + paintedFront).toImmutableList()
}
