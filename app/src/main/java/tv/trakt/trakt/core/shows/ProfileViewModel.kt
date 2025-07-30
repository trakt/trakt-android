package tv.trakt.trakt.core.shows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

internal class ShowsViewModel : ViewModel() {
    private val initialState = ShowsState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)

    init {
        loadBackground()
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString("mobile_background_image_url")
        backgroundState.update { configUrl }
    }

    val state: StateFlow<ShowsState> = combine(
        backgroundState,
    ) { state ->
        ShowsState(
            backgroundUrl = state[0],
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
