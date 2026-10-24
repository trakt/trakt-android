package tv.trakt.trakt.core.discover

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
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.calendar.model.CalendarSource
import tv.trakt.trakt.core.calendar.model.CalendarView
import tv.trakt.trakt.core.calendar.usecases.GetCalendarViewUseCase
import tv.trakt.trakt.core.discover.DiscoverState.UserState

@OptIn(FlowPreview::class)
@Suppress("UNCHECKED_CAST")
internal class DiscoverViewModel(
    private val sessionManager: SessionManager,
    private val collectionStateProvider: CollectionStateProvider,
    private val getCalendarViewUseCase: GetCalendarViewUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = DiscoverState()

    private val userState = MutableStateFlow(initialState.user)
    private val releasesViewState = MutableStateFlow(initialState.releasesView)

    init {
        observeUser()
        observeData()
        observeReleasesView()

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

    private fun observeReleasesView() {
        getCalendarViewUseCase.observeView(CalendarSource.Releases)
            .onEach { view -> releasesViewState.update { view } }
            .launchIn(viewModelScope)
    }

    fun onVipUpsellSeen() {
        analytics.vip.logUpsellImpression("discover")
    }

    val state = combine(
        userState,
        collectionStateProvider.stateFlow,
        releasesViewState,
    ) { state ->
        DiscoverState(
            user = state[0] as UserState,
            collection = state[1] as UserCollectionState,
            releasesView = state[2] as CalendarView,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
