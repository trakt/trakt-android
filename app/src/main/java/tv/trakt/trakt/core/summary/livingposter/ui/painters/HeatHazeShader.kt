package tv.trakt.trakt.core.summary.livingposter.ui.painters

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ShaderBrush

private const val HEAT_HAZE_AGSL = """
uniform shader backdrop;
uniform float unit;
uniform float seconds;
uniform float top;
uniform float bottom;
uniform float amplitude;

half4 main(float2 position) {
    float depth = clamp((position.y - top) / (bottom - top), 0.0, 1.0);
    float reach = depth * depth * amplitude;
    float row = position.y / unit;
    float dx = reach * (sin(row * 0.19 - seconds * 6.1) * 0.6 + sin(row * 0.053 + seconds * 3.7) * 0.4);
    float dy = reach * 0.5 * sin(row * 0.11 - seconds * 4.3);
    return backdrop.eval(float2(position.x - dx, position.y + dy));
}
"""

/** Ripples the background in one pass: each pixel samples the backdrop at a wave-displaced position. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class HeatHazeShader {
    private val shader = RuntimeShader(HEAT_HAZE_AGSL)
    private val coverMatrix = Matrix()
    private var backdrop: Bitmap? = null
    private var backdropShader: BitmapShader? = null

    /** A brush for the band from [top] to [bottom], with [backdrop] drawn to cover [canvasSize]. */
    fun brush(
        backdrop: Bitmap,
        canvasSize: Size,
        unit: Float,
        seconds: Float,
        top: Float,
        bottom: Float,
        amplitude: Float,
    ): Brush {
        val imageShader = backdropShader
            ?.takeIf { this.backdrop === backdrop }
            ?: BitmapShader(backdrop, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).also {
                this.backdrop = backdrop
                backdropShader = it
            }

        val scale = maxOf(canvasSize.width / backdrop.width, canvasSize.height / backdrop.height)
        coverMatrix.setScale(scale, scale)
        coverMatrix.postTranslate(
            (canvasSize.width - backdrop.width * scale) / 2F,
            (canvasSize.height - backdrop.height * scale) / 2F,
        )
        imageShader.setLocalMatrix(coverMatrix)

        shader.setInputShader("backdrop", imageShader)
        shader.setFloatUniform("unit", unit)
        shader.setFloatUniform("seconds", seconds)
        shader.setFloatUniform("top", top)
        shader.setFloatUniform("bottom", bottom)
        shader.setFloatUniform("amplitude", amplitude)

        return ShaderBrush(shader)
    }
}
