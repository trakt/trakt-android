package tv.trakt.trakt.core.summary.livingposter.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import tv.trakt.trakt.core.summary.livingposter.model.RotationRate

/** Gyroscope rates mapped to the screen axes for [displayRotation]. Empty without a gyroscope. */
internal fun Context.screenRotationRates(displayRotation: Int): Flow<RotationRate> {
    return callbackFlow {
        val sensorManager = getSystemService<SensorManager>()
        val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (sensorManager == null || gyroscope == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val deviceX = event.values[0]
                val deviceY = event.values[1]
                val (screenX, screenY) = when (displayRotation) {
                    Surface.ROTATION_90 -> -deviceY to deviceX
                    Surface.ROTATION_180 -> -deviceX to -deviceY
                    Surface.ROTATION_270 -> deviceY to -deviceX
                    else -> deviceX to deviceY
                }
                trySend(RotationRate(x = screenX, y = screenY, timestampNanos = event.timestamp))
            }

            override fun onAccuracyChanged(
                sensor: Sensor,
                accuracy: Int,
            ) = Unit
        }

        sensorManager.registerListener(listener, gyroscope, SensorManager.SENSOR_DELAY_GAME)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
