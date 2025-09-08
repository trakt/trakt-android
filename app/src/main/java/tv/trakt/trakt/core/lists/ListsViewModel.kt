package tv.trakt.trakt.core.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.lists.ListsState.UserState

internal class ListsViewModel(
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = ListsState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val userState = MutableStateFlow(initialState.user)

    init {
        loadBackground()
        observeUser()
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString("mobile_background_image_url")
        backgroundState.update { configUrl }
    }

    private fun observeUser() {
        viewModelScope.launch {
            sessionManager.observeProfile()
                .collect { user ->
                    userState.update {
                        UserState(
                            user = user,
                            loading = LoadingState.DONE,
                        )
                    }
                }
        }
    }

    val state: StateFlow<ListsState> = combine(
        backgroundState,
        userState,
    ) { s1, s2 ->
        ListsState(
            backgroundUrl = s1,
            user = s2,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
