package tv.trakt.trakt.core.main.ui.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.core.main.model.MaintenanceMode
import tv.trakt.trakt.core.main.usecases.MaintenanceModeUseCase

@Suppress("UNCHECKED_CAST")
internal class MaintenanceViewModel(
    private val maintenanceModeUseCase: MaintenanceModeUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = MaintenanceState()

    private val maintenanceState = MutableStateFlow(initialState.maintenance)

    init {
        observeMaintenance()
    }

    private fun observeMaintenance() {
        maintenanceModeUseCase.observe()
            .onEach { mode ->
                if (mode != null && maintenanceState.value == null) {
                    analytics.logScreenView(screenName = "maintenance")
                }
                maintenanceState.update { mode }
            }
            .launchIn(viewModelScope)
    }

    val state = combine(
        maintenanceState,
    ) { state ->
        MaintenanceState(
            maintenance = state[0] as MaintenanceMode?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
