package tv.trakt.trakt.core.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.discover.DiscoverState.UserState
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.user.CollectionStateProvider

@OptIn(FlowPreview::class)
internal class DiscoverViewModel(
    private val modeManager: MediaModeManager,
    private val sessionManager: SessionManager,
    private val collectionStateProvider: CollectionStateProvider,
    private val checkInManager: CheckInManager,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = DiscoverState()

    private val modeState = MutableStateFlow(modeManager.getMode())
    private val userState = MutableStateFlow(initialState.user)
    private val checkInState = MutableStateFlow(initialState.checkIn)

    init {
        observeUser()
        observeMode()
        observeData()
        observeCheckIn()

        analytics.logScreenView(
            screenName = "discover",
        )
    }

    private fun observeMode() {
        modeManager.observeMode()
            .onEach { value ->
                modeState.update { value }
            }
            .launchIn(viewModelScope)
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .onEach { user ->
                userState.update {
                    UserState(
                        user = user,
                        loading = LoadingState.DONE,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeData() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun observeCheckIn() {
        checkInManager.observe()
            .debounce(200)
            .onEach { checkIn ->
                checkInState.update {
                    checkIn.isActive()
                }
            }
            .launchIn(viewModelScope)
    }

    val state = combine(
        userState,
        collectionStateProvider.stateFlow,
        checkInState,
    ) { s1, s2, s3 ->
        DiscoverState(
            user = s1,
            collection = s2,
            checkIn = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
