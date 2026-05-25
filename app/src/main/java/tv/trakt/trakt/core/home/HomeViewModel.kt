package tv.trakt.trakt.core.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.home.HomeState.UserState

@OptIn(FlowPreview::class)
internal class HomeViewModel(
    private val filterManager: GlobalFilterManager,
    private val sessionManager: SessionManager,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = HomeState()
    private val initialMode = filterManager.getFilter().mode

    private val modeState = MutableStateFlow(initialMode)
    private val userState = MutableStateFlow(initialState.user)

    init {
        observeUser()
        observeMode()

        analytics.logScreenView(screenName = "home")
        analytics.logMediaMode(mode = initialMode.name)
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .onEach { user ->
                userState.update {
                    UserState(
                        user = user,
                        loading = LoadingState.Done,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeMode() {
        filterManager.observeFilter()
            .onEach { value ->
                modeState.update { value.mode }
            }
            .launchIn(viewModelScope)
    }

    val state = combine(
        modeState,
        userState,
    ) { s1, s2 ->
        HomeState(
            mode = s1,
            user = s2,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
