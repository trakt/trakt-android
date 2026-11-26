package tv.trakt.trakt.core.summary.livingposter.ui.painters

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane
import tv.trakt.trakt.core.summary.livingposter.ui.toPixelRect
import tv.trakt.trakt.core.summary.livingposter.ui.withLayer

private const val TILE_SCALE = 1.2F
private val mistColor = Color(200, 212, 220)

private class FogBank(
    val seed: Int,
    val from: Float,
    val to: Float,
    val speed: Float,
    val opacity: Float,
    val top: Float,
)

private val backBank = FogBank(seed = 13, from = 0.4F, to = 0.85F, speed = 8F, opacity = 0.6F, top = 0.55F)
private val frontBank = FogBank(seed = 17, from = 0.45F, to = 0.9F, speed = -12F, opacity = 0.4F, top = 0.72F)

/** Two cloud banks drifting in opposite directions, one behind and one in front of the figures. */
internal class FogPainter(
    private val effect: LivingPosterEffect,
) : EffectPainter {
    private val back = BankPainter(backBank)
    private val front = BankPainter(frontBank)

    override fun DrawScope.paint(
        plane: EffectPlane,
        scene: EffectScene,
    ) {
        when (plane) {
            EffectPlane.Back -> with(back) { paint(scene) }
            EffectPlane.Front -> with(front) { paint(scene) }
            EffectPlane.Backdrop, EffectPlane.Rim -> Unit
        }
    }

    private inner class BankPainter(
        private val bank: FogBank,
    ) {
        private val opacity = (bank.opacity * intensityScale(effect.intensity)).coerceAtMost(1F)
        private val texture: ImageBitmap by lazy {
            cloudTexture(seed = bank.seed, color = mistColor, from = bank.from, to = bank.to)
        }

        fun DrawScope.paint(scene: EffectScene) {
            val box = effect.box.toPixelRect(size)
            if (box.isEmpty) return

            clipRect(box.left, box.top, box.right, box.bottom) {
                withLayer(bounds = box) {
                    drawTiled(offset = scene.frame.seconds * bank.speed * scene.unit)
                    drawRect(
                        brush = Brush.verticalGradient(
                            0F to Color.Black.copy(alpha = 0F),
                            0.5F to Color.Black.copy(alpha = 0.8F),
                            1F to Color.Black,
                            startY = box.top + box.height * bank.top,
                            endY = box.bottom,
                        ),
                        blendMode = BlendMode.DstIn,
                    )
                }
            }
        }

        private fun DrawScope.drawTiled(offset: Float) {
            val tile = size.width * TILE_SCALE
            val start = ((offset % tile) + tile) % tile
            val columns = generateSequence(-start) { it + tile }.takeWhile { it < size.width }
            val rows = generateSequence(0F) { it + tile }.takeWhile { it < size.height }

            columns.forEach { x ->
                rows.forEach { y ->
                    drawImageIn(texture, Offset(x, y), Size(tile, tile), alpha = opacity)
                }
            }
        }
    }
}
