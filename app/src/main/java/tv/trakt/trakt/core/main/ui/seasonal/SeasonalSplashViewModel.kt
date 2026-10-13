package tv.trakt.trakt.core.main.ui.seasonal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tv.trakt.trakt.core.settings.usecases.SeasonalThemeUseCase
import tv.trakt.trakt.ui.theme.model.SeasonalTheme

@Suppress("UNCHECKED_CAST")
internal class SeasonalSplashViewModel(
    private val seasonalThemeUseCase: SeasonalThemeUseCase,
) : ViewModel() {
    private val initialState = SeasonalSplashState()

    fun dismiss(season: SeasonalTheme) {
        viewModelScope.launch {
            seasonalThemeUseCase.markSplashShown(season)
        }
    }

    val state = combine(
        seasonalThemeUseCase.observePendingSplash(),
    ) { state ->
        SeasonalSplashState(
            season = state[0],
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
