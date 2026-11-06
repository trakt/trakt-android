package tv.trakt.trakt.core.settings.categories.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.core.settings.usecases.ThemeModeUseCase
import tv.trakt.trakt.ui.theme.model.ThemeMode

@Suppress("UNCHECKED_CAST")
internal class AppearanceSettingsViewModel(
    private val themeModeUseCase: ThemeModeUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = AppearanceSettingsState()

    private val themeModeState = MutableStateFlow(initialState.themeMode)

    init {
        observeThemeMode()

        analytics.logScreenView(
            screenName = "settings_appearance",
        )
    }

    private fun observeThemeMode() {
        themeModeUseCase.observeThemeMode()
            .onEach { mode ->
                themeModeState.update { mode }
            }
            .launchIn(viewModelScope)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeModeUseCase.setThemeMode(mode)
            analytics.logTheme(mode.name)
        }
    }

    val state = combine(
        themeModeState,
    ) { state ->
        AppearanceSettingsState(
            themeMode = state[0] as ThemeMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
