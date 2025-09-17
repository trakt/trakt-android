package tv.trakt.trakt.core.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.lists.ListsState.UserState
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase

internal class ListsViewModel(
    private val sessionManager: SessionManager,
    private val getPersonalListsUseCase: GetPersonalListsUseCase,
) : ViewModel() {
    private val initialState = ListsState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val userState = MutableStateFlow(initialState.user)
    private val listsState = MutableStateFlow(initialState.lists)
    private val listsLoadingState = MutableStateFlow(initialState.listsLoading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadBackground()
        loadData()
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            sessionManager.observeProfile()
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

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    fun loadData() {
        viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                val localItems = getPersonalListsUseCase.getLocalLists()
                if (localItems.isNotEmpty()) {
                    listsState.update { localItems }
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
