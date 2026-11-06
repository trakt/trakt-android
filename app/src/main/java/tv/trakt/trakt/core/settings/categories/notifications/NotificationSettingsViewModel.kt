package tv.trakt.trakt.core.settings.categories.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.notifications.data.work.ScheduleNotificationsWorker
import tv.trakt.trakt.core.notifications.data.work.mir.ScheduleMonthReviewNotificationWorker
import tv.trakt.trakt.core.notifications.model.DeliveryAdjustment
import tv.trakt.trakt.core.notifications.usecases.EnableNotificationsUseCase
import tv.trakt.trakt.core.notifications.usecases.UpdateNotificationsDeliveryUseCase

@Suppress("UNCHECKED_CAST")
internal class NotificationSettingsViewModel(
    private val appContext: Context,
    private val enableNotificationsUseCase: EnableNotificationsUseCase,
    private val updateNotificationsDeliveryUseCase: UpdateNotificationsDeliveryUseCase,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = NotificationSettingsState()

    private val enabledState = MutableStateFlow(initialState.enabled)
    private val deliveryState = MutableStateFlow(initialState.delivery)

    init {
        loadSettings()

        analytics.logScreenView(
            screenName = "settings_notifications",
        )
    }

    private fun loadSettings() {
        viewModelScope.launch {
            enabledState.update {
                enableNotificationsUseCase.isNotificationsEnabled()
            }
            deliveryState.update {
                updateNotificationsDeliveryUseCase.getDeliveryTime()
            }
        }
    }

    fun enableNotifications(enable: Boolean) {
        viewModelScope.launch {
            try {
                enabledState.update {
                    enableNotificationsUseCase.enableNotifications(enable)
                }

                if (enable) {
                    ScheduleNotificationsWorker.schedule(appContext)
                    ScheduleMonthReviewNotificationWorker.schedule(appContext)
                } else {
                    ScheduleNotificationsWorker.clear(appContext)
                    ScheduleMonthReviewNotificationWorker.clear(appContext)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun setDeliveryTime(value: DeliveryAdjustment) {
        viewModelScope.launch {
            try {
                deliveryState.update {
                    updateNotificationsDeliveryUseCase.setDeliveryTime(value)
                }

                if (enabledState.value) {
                    ScheduleNotificationsWorker.schedule(appContext)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    val state = combine<Any?, NotificationSettingsState>(
        enabledState,
        deliveryState,
    ) { state ->
        NotificationSettingsState(
            enabled = state[0] as Boolean,
            delivery = state[1] as DeliveryAdjustment?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
