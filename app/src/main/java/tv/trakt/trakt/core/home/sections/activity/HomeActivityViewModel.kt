package tv.trakt.trakt.core.home.sections.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter.PERSONAL
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter.SOCIAL
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.usecases.GetActivityFilterUseCase
import tv.trakt.trakt.core.home.sections.activity.usecases.GetPersonalActivityUseCase
import tv.trakt.trakt.core.home.sections.activity.usecases.GetSocialActivityUseCase

internal class HomeActivityViewModel(
    private val getActivityFilterUseCase: GetActivityFilterUseCase,
    private val getSocialActivityUseCase: GetSocialActivityUseCase,
    private val getPersonalActivityUseCase: GetPersonalActivityUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = HomeActivityState()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadDataJob: Job? = null

    init {
        loadData()
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            userState.update { sessionManager.getProfile() }
            sessionManager.observeProfile()
                .collect { user ->
                    if (userState.value != user) {
                        userState.update { user }
                        loadData()
                    }
                }
        }
    }

    private fun loadData() {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val filter = loadFilter()
                val localItems = when (filter) {
                    SOCIAL -> getSocialActivityUseCase.getLocalSocialActivity()
                    PERSONAL -> getPersonalActivityUseCase.getLocalPersonalActivity()
                }

                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    when (filter) {
                        SOCIAL -> getSocialActivityUseCase.getSocialActivity(HOME_SECTION_LIMIT)
                        PERSONAL -> getPersonalActivityUseCase.getPersonalActivity(1, HOME_SECTION_LIMIT)
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Error loading social activity")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<HomeActivityItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        }

        return false
    }

    private suspend fun loadFilter(): HomeActivityFilter {
        val filter = getActivityFilterUseCase.getFilter()
        filterState.update { filter }
        return filter
    }

    fun setFilter(newFilter: HomeActivityFilter) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            getActivityFilterUseCase.setFilter(newFilter)
            loadFilter()
            loadData()
        }
    }

    val state: StateFlow<HomeActivityState> = combine(
        loadingState,
        itemsState,
        filterState,
        errorState,
        userState,
    ) { s0, s1, s2, s3, s4 ->
        HomeActivityState(
            loading = s0,
            items = s1,
            filter = s2,
            error = s3,
            user = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
