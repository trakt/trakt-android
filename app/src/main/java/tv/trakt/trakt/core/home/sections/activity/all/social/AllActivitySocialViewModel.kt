package tv.trakt.trakt.core.home.sections.activity.all.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.home.HomeConfig.HOME_ALL_ACTIVITY_LIMIT
import tv.trakt.trakt.core.home.sections.activity.all.AllActivityState
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.usecases.GetSocialActivityUseCase

internal class AllActivitySocialViewModel(
    private val getActivityUseCase: GetSocialActivityUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = AllActivityState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val itemsState = MutableStateFlow(initialState.items)
    private val usersFilterState = MutableStateFlow(initialState.usersFilter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(IDLE)
    private val errorState = MutableStateFlow(initialState.error)

    private var pages: Int = 1
    private var hasMoreData: Boolean = false

    init {
        loadBackground()
        loadData()
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        clear()
        viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                val localItems = getActivityUseCase.getLocalSocialActivity(
                    limit = HOME_ALL_ACTIVITY_LIMIT,
                )
                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadUsersFilter(localItems)
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                val remoteItems = getActivityUseCase.getSocialActivity(
                    page = 1,
                    limit = HOME_ALL_ACTIVITY_LIMIT,
                )
                itemsState.update { remoteItems }
                loadUsersFilter(remoteItems)

                hasMoreData = remoteItems.size >= HOME_ALL_ACTIVITY_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.w(error, "Error loading social activity")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private fun loadUsersFilter(items: List<HomeActivityItem>) {
        val users = items
            .groupBy { it.user }
            .map { (user, items) -> user to items.size }
            .sortedByDescending { it.second }
            .mapNotNull { it.first }
            .take(10)
            .toImmutableList()

        if (users.size < 3) {
            return
        }

        usersFilterState.update {
            AllActivityState.UsersFilter(
                users = users,
                selectedUser = null,
            )
        }
    }

//    fun loadMoreData() {
//        if (itemsState.value.isNullOrEmpty() || !hasMoreData) {
//            return
//        }
//
//        if (loadingMoreState.value.isLoading || loadingState.value.isLoading) {
//            return
//        }
//
//        viewModelScope.launch {
//            try {
//                loadingMoreState.update { LOADING }
//
//                val nextData = getActivityUseCase.getSocialActivity(
//                    page = pages + 1,
//                    limit = HOME_ALL_ACTIVITY_LIMIT,
//                )
//
//                itemsState.update { items ->
//                    items
//                        ?.plus(nextData)
//                        ?.distinctBy { it.id }
//                        ?.toImmutableList()
//                }
//
//                pages += 1
//                hasMoreData = nextData.size >= HOME_ALL_ACTIVITY_LIMIT
//            } catch (error: Exception) {
//                error.rethrowCancellation {
//                    errorState.update { error }
//                    Timber.w(error, "Failed to load more page data")
//                }
//            } finally {
//                loadingMoreState.update { DONE }
//            }
//        }
//    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<HomeActivityItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        } else {
            itemsState.update { null }
            loadingState.update { IDLE }
        }

        return false
    }

    private fun clear() {
        pages = 1
        hasMoreData = true
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<AllActivityState> = combine(
        backgroundState,
        itemsState,
        usersFilterState,
        loadingState,
        loadingMoreState,
        errorState,
    ) { state ->
        AllActivityState(
            backgroundUrl = state[0] as String,
            items = state[1] as ImmutableList<HomeActivityItem>?,
            usersFilter = state[2] as AllActivityState.UsersFilter,
            loading = state[3] as LoadingState,
            loadingMore = state[4] as LoadingState,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
