package tv.trakt.trakt.core.summary.livingposter.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.livingposter.LivingPoster
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect.Kind

private const val BEHIND_SHIFT = -0.15F
private const val FLARE_SHIFT = -0.6F
private const val FRONT_SHIFT = 1F

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
    data class Particles(
        override val key: String,
        override val shift: Float,
        val effects: ImmutableList<LivingPosterEffect>,
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

private val LivingPosterEffect.isAmbientBehind: Boolean
    get() = kind == Kind.Fog || kind == Kind.Haze || kind == Kind.Flicker || kind == Kind.Glow

private val LivingPosterEffect.isOnBackground: Boolean
    get() = kind == Kind.Bulbs || (isMasked && mask == LivingPosterEffect.MaskSource.Letters)

private val LivingPosterEffect.isTitleMasked: Boolean
    get() = isMasked && mask == LivingPosterEffect.MaskSource.Title

private val LivingPosterEffect.isScreenBlended: Boolean
    get() = kind == Kind.Embers || kind == Kind.Fireflies

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

/** Orders layers and effects back to front: ambient effects and particles sit behind the figures. */
internal fun LivingPoster.toStack(): ImmutableList<LivingPosterStackItem> {
    val particles = effects.filter { it.isParticle }
    val ambient = effects.filter { it.isAmbientBehind }
    val onBackground = effects.filter { it.isOnBackground }
    val front = effects.filter { !it.isParticle && !it.isAmbientBehind && !it.isOnBackground }

    val particleGroups = listOf(
        LivingPosterStackItem.Particles(
            key = "particles-screen",
            shift = BEHIND_SHIFT,
            effects = particles.filter { it.isScreenBlended }.toImmutableList(),
            isScreen = true,
        ),
        LivingPosterStackItem.Particles(
            key = "particles-normal",
            shift = BEHIND_SHIFT,
            effects = particles.filterNot { it.isScreenBlended }.toImmutableList(),
            isScreen = false,
        ),
    ).filter { it.effects.isNotEmpty() }

    val behind = ambient.mapIndexed { index, effect ->
        toEffectItem(effect, key = "behind-${effect.kind.value}-$index", shift = BEHIND_SHIFT)
    } + particleGroups

    val backgroundShift = layers.firstOrNull()?.shift ?: 0F
    val titleShift = layers
        .withIndex()
        .lastOrNull { (index, layer) -> index > 0 && !isBelowTitle(layer.shift) }
        ?.value
        ?.shift
        ?: FRONT_SHIFT

    fun frontShift(effect: LivingPosterEffect): Float {
        return when {
            effect.kind == Kind.Flare -> FLARE_SHIFT
            effect.isTitleMasked -> titleShift
            else -> FRONT_SHIFT
        }
    }

    val figuresIndex = layers.indexOfFirst {
        LivingPosterLayerRole.fromShift(it.shift) == LivingPosterLayerRole.Figures
    }
    val behindIndex = when {
        figuresIndex >= 0 -> figuresIndex
        layers.size > 1 -> layers.size - 1
        else -> layers.size
    }

    val stack = layers.flatMapIndexed { index, layer ->
        val layerItem = LivingPosterStackItem.Layer(
            key = "layer-$index",
            shift = layer.shift,
            url = layer.url,
            isCutout = isCutoutLayer(layer.shift, index, layers.size),
        )
        val backgroundEffects = when (index) {
            0 -> onBackground.mapIndexed { position, effect ->
                toEffectItem(effect, key = "background-${effect.kind.value}-$position", shift = backgroundShift)
            }

            else -> emptyList()
        }

        (if (index == behindIndex) behind else emptyList()) + layerItem + backgroundEffects
    }

    val trailing = if (behindIndex == layers.size) behind else emptyList()
    val frontItems = front.mapIndexed { index, effect ->
        toEffectItem(effect, key = "front-${effect.kind.value}-$index", shift = frontShift(effect))
    }

    return (stack + trailing + frontItems).toImmutableList()
}
