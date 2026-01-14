package tv.trakt.trakt.core.notifications.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import tv.trakt.trakt.core.notifications.model.DeliveryAdjustment

private val KEY_NOTIFICATION_DELIVERY_TIME = stringPreferencesKey("key_notification_delivery_time")

internal class UpdateNotificationsDeliveryUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun setDeliveryTime(value: DeliveryAdjustment): DeliveryAdjustment {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_NOTIFICATION_DELIVERY_TIME] = value.name
            }
        }

        return value
    }

    suspend fun getDeliveryTime(): DeliveryAdjustment {
        val preferences = dataStore.data.map { prefs ->
            val name = prefs[KEY_NOTIFICATION_DELIVERY_TIME] ?: DeliveryAdjustment.DISABLED.name
            DeliveryAdjustment.valueOf(name)
        }
        return preferences.first()
    }
}
