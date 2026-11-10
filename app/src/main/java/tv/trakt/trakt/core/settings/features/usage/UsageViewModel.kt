package tv.trakt.trakt.core.settings.features.usage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.billing.model.VipUsageGroup
import tv.trakt.trakt.core.billing.usecases.GetVipUsageLimitsUseCase
import tv.trakt.trakt.core.settings.data.observeSettingsProfile

@Suppress("UNCHECKED_CAST")
internal class UsageViewModel(
    private val getUsageLimitsUseCase: GetVipUsageLimitsUseCase,
    private val sessionManager: SessionManager,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = UsageState()

    private val userState = MutableStateFlow(initialState.user)
    private val groupsState = MutableStateFlow(initialState.groups)
    private val loadingState = MutableStateFlow(initialState.loading)

    init {
        loadUser()
        loadUsage()

        analytics.logScreenView(
            screenName = "vip_usage",
        )
    }

    private fun loadUser() {
        sessionManager.observeSettingsProfile()
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
    }

    private fun loadUsage() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }
                groupsState.update { getUsageLimitsUseCase.getGroupedLimits() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    val state = combine(
        userState,
        groupsState,
        loadingState,
    ) { state ->
        UsageState(
            user = state[0] as User?,
            groups = state[1] as ImmutableList<VipUsageGroup>?,
            loading = state[2] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
