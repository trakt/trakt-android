package tv.trakt.trakt.core.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.discover.DiscoverState.UserState
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.user.CollectionStateProvider

@OptIn(FlowPreview::class)
internal class DiscoverViewModel(
    private val modeManager: MediaModeManager,
    private val sessionManager: SessionManager,
    private val collectionStateProvider: CollectionStateProvider,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = DiscoverState()

    private val modeState = MutableStateFlow(modeManager.getMode())
    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val userState = MutableStateFlow(initialState.user)

    init {
        loadBackground()

        observeUser()
        observeMode()
        observeData()

        analytics.logScreenView(
            screenName = "discover",
        )
    }

    private fun loadBackground() {
        val backgroundUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { backgroundUrl }
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

    val state = combine(
        backgroundState,
        userState,
        collectionStateProvider.stateFlow,
    ) { s1, s2, s3 ->
        DiscoverState(
            backgroundUrl = s1,
            user = s2,
            collection = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
