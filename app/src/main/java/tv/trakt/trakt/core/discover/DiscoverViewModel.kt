package tv.trakt.trakt.core.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableMap
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
import tv.trakt.trakt.core.discover.DiscoverState.UserState
import tv.trakt.trakt.core.user.CollectionStateProvider
import tv.trakt.trakt.helpers.editscreen.data.EditScreenManager
import tv.trakt.trakt.helpers.editscreen.data.model.EditScreenKey.Companion.DiscoverKeys

@OptIn(FlowPreview::class)
internal class DiscoverViewModel(
    private val sessionManager: SessionManager,
    private val editScreenManager: EditScreenManager,
    private val collectionStateProvider: CollectionStateProvider,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = DiscoverState()

    private val userState = MutableStateFlow(initialState.user)
    private val visibilityState = MutableStateFlow(initialState.visibility)

    init {
        observeUser()
        observeData()
        observeVisibility()

        analytics.logScreenView(
            screenName = "discover",
        )
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

    private fun observeData() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun observeVisibility() {
        editScreenManager.observe(DiscoverKeys)
            .onEach { visibility ->
                visibilityState.update {
                    visibility.toImmutableMap()
                }
            }
            .launchIn(viewModelScope)
    }

    val state = combine(
        userState,
        visibilityState,
        collectionStateProvider.stateFlow,
    ) { s1, s2, s3 ->
        DiscoverState(
            user = s1,
            visibility = s2,
            collection = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
