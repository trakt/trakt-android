package tv.trakt.trakt.core.lists.sections.personal.features.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
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
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_ALL_LIMIT
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.lists.sections.personal.features.all.navigation.ListsPersonalDestination
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase

internal class AllPersonalListViewModel(
    savedStateHandle: SavedStateHandle,
    private val getListUseCase: GetPersonalListsUseCase,
    private val getListItemsUseCase: GetPersonalListItemsUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<ListsPersonalDestination>()

    private val initialState = AllPersonalListState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val listState = MutableStateFlow(initialState.list)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadDataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadBackground()
        loadDetails()
        loadData()
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    fun loadDetails() {
        viewModelScope.launch {
            listState.update {
                getListUseCase.getLocalList(destination.listId.toTraktId())
            }
        }
    }

    fun loadData(
        ignoreErrors: Boolean = false,
        localOnly: Boolean = false,
    ) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val localItems = getListItemsUseCase.getLocalItems(destination.listId.toTraktId())
                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                    if (localOnly) {
                        return@launch
                    }
                } else {
                    loadingState.update { LOADING }
                }

                if (localItems.size >= LISTS_SECTION_LIMIT) {
                    itemsState.update {
                        getListItemsUseCase.getItems(
                            listId = destination.listId.toTraktId(),
                            limit = LISTS_ALL_LIMIT,
                        )
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.w(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<PersonalListItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        }

        return false
    }

    fun removeItem(item: PersonalListItem?) {
        val currentItems = itemsState.value ?: return
        itemsState.update {
            currentItems
                .filterNot { it.key == item?.key }
                .toImmutableList()
        }
    }

    override fun onCleared() {
        loadDataJob?.cancel()
        processingJob?.cancel()
        super.onCleared()
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<AllPersonalListState> = combine(
        loadingState,
        listState,
        itemsState,
        errorState,
        backgroundState,
    ) { state ->
        AllPersonalListState(
            loading = state[0] as LoadingState,
            list = state[1] as? CustomList,
            items = state[2] as? ImmutableList<PersonalListItem>,
            error = state[3] as? Exception,
            backgroundUrl = state[4] as? String,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
