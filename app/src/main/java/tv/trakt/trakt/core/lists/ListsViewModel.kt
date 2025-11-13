package tv.trakt.trakt.core.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.lists.ListsState.UserState
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase

@OptIn(FlowPreview::class)
internal class ListsViewModel(
    private val sessionManager: SessionManager,
    private val getPersonalListsUseCase: GetPersonalListsUseCase,
    private val localListsSource: ListsPersonalLocalDataSource,
    private val localListsItemsSource: ListsPersonalItemsLocalDataSource,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = ListsState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val userState = MutableStateFlow(initialState.user)
    private val listsState = MutableStateFlow(initialState.lists)
    private val listsLoadingState = MutableStateFlow(initialState.listsLoading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadBackground()
        observeUser()
        observeLists()

        analytics.logScreenView(
            screenName = "lists",
        )
    }

    private fun observeUser() {
        viewModelScope.launch {
            sessionManager.observeProfile()
                .distinctUntilChanged()
                .collect { user ->
                    userState.update {
                        UserState(
                            user = user,
                            loading = DONE,
                        )
                    }
                    loadData()
                }
        }
    }

    private fun observeLists() {
        merge(
            localListsSource.observeUpdates(),
            localListsItemsSource.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadLocalData()
            }
            .launchIn(viewModelScope)
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    private fun loadLocalData() {
        viewModelScope.launch {
            try {
                val localLists = getPersonalListsUseCase.getLocalLists()
                listsState.update { localLists }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.value = error
                }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                val localLists = getPersonalListsUseCase.getLocalLists()
                if (localLists.isNotEmpty()) {
                    listsState.update { localLists }
                    listsLoadingState.update { DONE }
                } else {
                    listsLoadingState.update { LOADING }
                }

                val lists = getPersonalListsUseCase.getLists()
                listsState.update { lists }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.value = error
                }
            } finally {
                listsLoadingState.update { DONE }
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            listsState.update {
                emptyList<CustomList>().toImmutableList()
            }
            listsLoadingState.update { DONE }
            return true
        } else {
            listsState.update { null }
            listsLoadingState.update { IDLE }
        }

        return false
    }

    val state: StateFlow<ListsState> = combine(
        backgroundState,
        userState,
        listsState,
        listsLoadingState,
        errorState,
    ) { s1, s2, s3, s4, s5 ->
        ListsState(
            backgroundUrl = s1,
            user = s2,
            lists = s3,
            listsLoading = s4,
            error = s5,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
