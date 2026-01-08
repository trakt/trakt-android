package tv.trakt.trakt.core.profile.sections.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter
import tv.trakt.trakt.core.profile.sections.social.usecases.GetSocialFilterUseCase
import tv.trakt.trakt.core.user.usecases.social.LoadUserSocialUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class ProfileSocialViewModel(
    private val loadSocialUseCase: LoadUserSocialUseCase,
    private val getFilterUseCase: GetSocialFilterUseCase,
    private val sessionManager: SessionManager,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = ProfileSocialState()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val collapseState = MutableStateFlow(isCollapsed())
    private val errorState = MutableStateFlow(initialState.error)
    private var loadDataJob: Job? = null
    private var collapseJob: Job? = null

    init {
        loadData()
    }

    fun loadData(ignoreErrors: Boolean = false) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                loadingState.update { LOADING }

                val filter = loadFilter()
                itemsState.update {
                    when (filter) {
                        SocialFilter.FOLLOWING -> loadSocialUseCase.loadFollowing()
                        SocialFilter.FOLLOWERS -> loadSocialUseCase.loadFollowers()
                    }.toImmutableList()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun loadFilter(): SocialFilter {
        val filter = getFilterUseCase.getFilter()
        filterState.update { filter }
        return filter
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<User>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        }

        return false
    }

    fun setFilter(newFilter: SocialFilter) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            getFilterUseCase.setFilter(newFilter)
            loadFilter()
            loadData()
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.PROFILE_SOCIAL)
                else -> collapsingManager.expand(CollapsingKey.PROFILE_SOCIAL)
            }
        }
    }

    private fun isCollapsed(): Boolean {
        return collapsingManager.isCollapsed(CollapsingKey.PROFILE_SOCIAL)
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ProfileSocialState> = combine(
        loadingState,
        itemsState,
        filterState,
        collapseState,
        errorState,
        userState,
    ) { states ->
        ProfileSocialState(
            loading = states[0] as LoadingState,
            items = states[1] as ImmutableList<User>?,
            filter = states[2] as SocialFilter,
            collapsed = states[3] as Boolean,
            error = states[4] as Exception?,
            user = states[5] as User?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
