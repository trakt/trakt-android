package tv.trakt.trakt.core.settings.categories.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.settings.data.observeSettingsProfile

@Suppress("UNCHECKED_CAST")
internal class CommunitySettingsViewModel(
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = CommunitySettingsState()

    private val userState = MutableStateFlow(initialState.user)

    init {
        loadUser()

        analytics.logScreenView(
            screenName = "settings_community",
        )
    }

    private fun loadUser() {
        sessionManager.observeSettingsProfile()
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
    }

    fun logRedditLink() {
        analytics.logLink(source = "settings", target = "reddit")
    }

    val state = combine(
        userState,
    ) { state ->
        CommunitySettingsState(
            user = state[0] as User?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
