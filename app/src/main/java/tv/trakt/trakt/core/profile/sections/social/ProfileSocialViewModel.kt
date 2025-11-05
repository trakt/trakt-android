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
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter
import tv.trakt.trakt.core.profile.sections.social.usecases.GetSocialFilterUseCase
import tv.trakt.trakt.core.user.usecases.social.LoadUserSocialUseCase

internal class ProfileSocialViewModel(
    private val loadSocialUseCase: LoadUserSocialUseCase,
    private val getFilterUseCase: GetSocialFilterUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = ProfileSocialState()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private var loadDataJob: Job? = null

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
                    Timber.e(error, "Failed to load data")
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

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ProfileSocialState> = combine(
        loadingState,
        itemsState,
        filterState,
        errorState,
        userState,
    ) { states ->
        ProfileSocialState(
            loading = states[0] as LoadingState,
            items = states[1] as ImmutableList<User>?,
            filter = states[2] as SocialFilter,
            error = states[3] as Exception?,
            user = states[4] as User?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
