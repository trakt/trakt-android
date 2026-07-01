@file:OptIn(FlowPreview::class)

package tv.trakt.trakt.core.profile.sections.social.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter.Followers
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter.Following
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter.Requests
import tv.trakt.trakt.core.profile.sections.social.usecases.GetSocialFilterUseCase
import tv.trakt.trakt.core.user.model.UserFollowRequest
import tv.trakt.trakt.core.user.usecases.social.LoadUserSocialRequestsUseCase
import tv.trakt.trakt.core.user.usecases.social.LoadUserSocialUseCase
import tv.trakt.trakt.core.user.usecases.social.updates.SocialUpdates
import kotlin.time.Duration.Companion.milliseconds

internal class AllProfileSocialViewModel(
    private val loadSocialUseCase: LoadUserSocialUseCase,
    private val loadRequestsUseCase: LoadUserSocialRequestsUseCase,
    private val getFilterUseCase: GetSocialFilterUseCase,
    private val socialUpdates: SocialUpdates,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = AllProfileSocialState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val requestsState = MutableStateFlow(initialState.requests)
    private val filterState = MutableStateFlow(initialState.filter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private var loadDataJob: Job? = null

    init {
        loadData()
        observeData()
    }

    private fun observeData() {
        socialUpdates.observeUpdates()
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                loadData(ignoreErrors = true, refreshRequests = true)
            }
            .launchIn(viewModelScope)
    }

    fun loadData(
        ignoreErrors: Boolean = false,
        refreshRequests: Boolean = true,
    ) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                loadingState.update { Loading }
                errorState.update { null }

                val filter = loadFilter()

                coroutineScope {
                    // Requests are filter-independent: only refetch on a real reload,
                    // not when the user toggles Following/Followers.
                    val requestsDeferred = async {
                        if (refreshRequests) {
                            runCatching { loadRequestsUseCase.loadRequests() }
                                .getOrDefault(EmptyImmutableList)
                        } else {
                            requestsState.value ?: EmptyImmutableList
                        }
                    }
                    val listDeferred = when (filter) {
                        Following -> async { loadSocialUseCase.loadFollowing() }
                        Followers -> async { loadSocialUseCase.loadFollowers() }
                        Requests -> async { EmptyImmutableList }
                    }

                    val requests = requestsDeferred.await()
                    requestsState.update { requests }

                    when (filter) {
                        Requests if requests.isEmpty() -> {
                            getFilterUseCase.setFilter(Following)
                            filterState.update { Following }
                            itemsState.update {
                                loadSocialUseCase
                                    .loadFollowing()
                                    .toImmutableList()
                            }
                        }
                        Requests -> {
                            itemsState.update {
                                requests.map(UserFollowRequest::user).toImmutableList()
                            }
                        }
                        else -> {
                            itemsState.update {
                                listDeferred.await().toImmutableList()
                            }
                        }
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
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
            itemsState.update { EmptyImmutableList }
            requestsState.update { EmptyImmutableList }
            loadingState.update { Done }
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
            loadData(refreshRequests = false)
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        loadingState,
        itemsState,
        filterState,
        errorState,
        requestsState,
    ) { states ->
        AllProfileSocialState(
            loading = states[0] as LoadingState,
            items = states[1] as ImmutableList<User>?,
            filter = states[2] as SocialFilter,
            error = states[3] as Exception?,
            requests = states[4] as ImmutableList<UserFollowRequest>?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
