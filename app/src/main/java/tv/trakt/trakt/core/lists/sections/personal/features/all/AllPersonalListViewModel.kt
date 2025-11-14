package tv.trakt.trakt.core.lists.sections.personal.features.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_ALL_LIMIT
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.lists.sections.personal.features.all.navigation.ListsPersonalDestination
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.user.CollectionStateProvider
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.core.user.data.local.UserListsLocalDataSource

@OptIn(FlowPreview::class)
internal class AllPersonalListViewModel(
    savedStateHandle: SavedStateHandle,
    modeManager: MediaModeManager,
    private val getListUseCase: GetPersonalListsUseCase,
    private val getListItemsUseCase: GetPersonalListItemsUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val userListLocalDataSource: UserListsLocalDataSource,
    private val collectionStateProvider: CollectionStateProvider,
    private val sessionManager: SessionManager,
    analytics: Analytics,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<ListsPersonalDestination>()

    private val initialState = AllPersonalListState()
    private val initialMode = modeManager.getMode()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val filterState = MutableStateFlow(initialMode)
    private val listState = MutableStateFlow(initialState.list)
    private val itemsState = MutableStateFlow(initialState.items)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadBackground()
        loadDetails()
        loadData()

        observeLists()
        observeCollection()

        analytics.logScreenView(
            screenName = "all_personal_list",
        )
    }

    private fun observeLists() {
        viewModelScope.launch {
            merge(
                userListLocalDataSource.observeUpdates(),
            )
                .distinctUntilChanged()
                .debounce(200)
                .collect {
                    loadData(
                        ignoreErrors = true,
                        localOnly = true,
                    )
                }
        }
    }

    private fun observeCollection() {
        collectionStateProvider
            .launchIn(viewModelScope)
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
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val localItems = getListItemsUseCase.getLocalItems(
                    listId = destination.listId.toTraktId(),
                    filter = filterState.value,
                )
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
                            filter = filterState.value,
                        )
                    }
                } else if (localItems.isEmpty()) {
                    itemsState.update { EmptyImmutableList }
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
                dataJob = null
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update { EmptyImmutableList }
            loadingState.update { DONE }
            return true
        }

        return false
    }

    fun setFilter(newFilter: MediaMode) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            filterState.update { newFilter }
            loadData(localOnly = true)
        }
    }

    fun removeItem(item: PersonalListItem?) {
        val currentItems = itemsState.value ?: return
        itemsState.update {
            currentItems
                .filterNot { it.key == item?.key }
                .toImmutableList()
        }
    }

    fun navigateToShow(show: Show) {
        if (navigateShow.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            navigateShow.update { show.ids.trakt }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            movieLocalDataSource.upsertMovies(listOf(movie))
            navigateMovie.update { movie.ids.trakt }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
    }

    override fun onCleared() {
        dataJob?.cancel()
        processingJob?.cancel()
        super.onCleared()
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        loadingState,
        filterState,
        listState,
        itemsState,
        collectionStateProvider.stateFlow,
        navigateShow,
        navigateMovie,
        errorState,
        backgroundState,
    ) { state ->
        AllPersonalListState(
            loading = state[0] as LoadingState,
            filter = state[1] as MediaMode?,
            list = state[2] as? CustomList,
            items = state[3] as? ImmutableList<PersonalListItem>,
            collection = state[4] as UserCollectionState,
            navigateShow = state[5] as? TraktId,
            navigateMovie = state[6] as? TraktId,
            error = state[7] as? Exception,
            backgroundUrl = state[8] as? String,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
