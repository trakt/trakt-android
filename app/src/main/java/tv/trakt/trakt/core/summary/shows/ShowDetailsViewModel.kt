package tv.trakt.trakt.core.summary.shows

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.home.sections.activity.all.data.local.AllActivityLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.usecases.AddPersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.summary.shows.features.seasons.data.local.ShowSeasonsLocalDataSource
import tv.trakt.trakt.core.summary.shows.navigation.ShowDetailsDestination
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowRatingsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowStudiosUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.usecase.lists.LoadUserListsUseCase
import tv.trakt.trakt.core.user.usecase.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecase.progress.LoadUserProgressUseCase
import tv.trakt.trakt.resources.R

@OptIn(FlowPreview::class)
internal class ShowDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getDetailsUseCase: GetShowDetailsUseCase,
    private val getExternalRatingsUseCase: GetShowRatingsUseCase,
    private val getShowStudiosUseCase: GetShowStudiosUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
    private val loadListsUseCase: LoadUserListsUseCase,
    private val updateShowHistoryUseCase: UpdateShowHistoryUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val updateShowWatchlistUseCase: UpdateShowWatchlistUseCase,
    private val addListItemUseCase: AddPersonalListItemUseCase,
    private val removeListItemUseCase: RemovePersonalListItemUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val activityLocalSource: AllActivityLocalDataSource,
    private val seasonsLocalDataSource: ShowSeasonsLocalDataSource,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<ShowDetailsDestination>()
    private val showId = destination.showId.toTraktId()

    private val initialState = ShowDetailsState()

    private val showState = MutableStateFlow(initialState.show)
    private val showRatingsState = MutableStateFlow(initialState.showRatings)
    private val showStudiosState = MutableStateFlow(initialState.showStudios)
    private val showProgressState = MutableStateFlow(initialState.showProgress)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingProgress = MutableStateFlow(initialState.loadingProgress)
    private val loadingLists = MutableStateFlow(initialState.loadingLists)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)
    private val userState = MutableStateFlow(initialState.user)

    init {
        loadUser()
        loadData()
        loadProgressData()
        observeData()
    }

    private fun observeData() {
        seasonsLocalDataSource.observeUpdates()
            .distinctUntilChanged()
            .debounce(250)
            .onEach {
                loadProgressData(
                    ignoreErrors = true,
                )
            }
            .launchIn(viewModelScope)
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                var show = getDetailsUseCase.getLocalShow(showId)
                if (show == null) {
                    loadingState.update { LOADING }
                    show = getDetailsUseCase.getShow(
                        showId = showId,
                    )
                }
                showState.update { show }

                loadRatings(show)
                loadStudios()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private fun loadProgressData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }
                loadingLists.update { LOADING }

                coroutineScope {
                    val progressAsync = async {
                        if (!loadProgressUseCase.isShowsLoaded()) {
                            loadProgressUseCase.loadShowsProgress()
                        }
                    }
                    val watchlistAsync = async {
                        if (!loadWatchlistUseCase.isShowsLoaded()) {
                            loadWatchlistUseCase.loadWatchlist()
                        }
                    }
                    val listsAsync = async {
                        if (!loadListsUseCase.isLoaded()) {
                            loadListsUseCase.loadLists()
                        }
                    }
                    progressAsync.await()
                    watchlistAsync.await()
                    listsAsync.await()
                }

                coroutineScope {
                    val progressAsync = async {
                        loadProgressUseCase.loadLocalShows()
                            .firstOrNull {
                                it.show.ids.trakt == showId
                            }
                    }

                    val watchlistAsync = async {
                        loadWatchlistUseCase.loadLocalShows()
                            .firstOrNull {
                                it.show.ids.trakt == showId
                            }
                    }

                    val listsAsync = async {
                        loadListsUseCase.loadLocalLists()
                            .values
                            .flatten()
                            .firstOrNull {
                                it.type == SHOW && it.id == showId
                            }
                    }

                    val progress = progressAsync.await()
                    val watchlist = watchlistAsync.await()
                    val lists = listsAsync.await()

                    showProgressState.update {
                        ShowDetailsState.ProgressState(
                            aired = progress?.progress?.aired ?: 0,
                            plays = progress?.progress?.plays,
                            inWatchlist = watchlist != null,
                            inLists = lists != null,
                        )
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.w(error)
                }
            } finally {
                loadingProgress.update { DONE }
                loadingLists.update { DONE }
            }
        }
    }

    private fun loadRatings(show: Show?) {
        if (show?.released?.isNowOrBefore() != true) {
            // Don't load ratings for unreleased shows
            return
        }
        viewModelScope.launch {
            try {
                showRatingsState.update {
                    getExternalRatingsUseCase.getExternalRatings(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    private fun loadStudios() {
        viewModelScope.launch {
            try {
                showStudiosState.update {
                    getShowStudiosUseCase.getStudios(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    fun toggleWatchlist() {
        if (showState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading ||
            loadingLists.value.isLoading
        ) {
            return
        }

        if (showProgressState.value?.inWatchlist == true) {
            removeFromWatchlist()
        } else {
            addToWatchlist()
        }
    }

    fun toggleList(
        listId: TraktId,
        add: Boolean,
    ) {
        if (showState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading ||
            loadingLists.value.isLoading
        ) {
            return
        }

        when {
            add -> addToList(listId)
            else -> removeFromList(listId)
        }
    }

    fun addToWatched() {
        if (showState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading ||
            loadingLists.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }

                updateShowHistoryUseCase.addToWatched(showId)
                val progress = loadProgressUseCase.loadShowsProgress()
                    .firstOrNull {
                        it.show.ids.trakt == showId
                    }
                userWatchlistLocalSource.removeShows(
                    ids = setOf(showId),
                    notify = true,
                )
                activityLocalSource.notifyUpdate()

                showProgressState.update {
                    it?.copy(
                        aired = progress?.progress?.aired ?: 0,
                        plays = progress?.progress?.plays ?: 0,
                        inWatchlist = false,
                    )
                }

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_added)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingProgress.update { DONE }
            }
        }
    }

    fun removeFromWatched(playId: Long) {
        if (showState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading ||
            loadingLists.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }

                updateEpisodeHistoryUseCase.removePlayFromHistory(playId)
                val progress = loadProgressUseCase.loadShowsProgress()
                    .firstOrNull {
                        it.show.ids.trakt == showId
                    }
                activityLocalSource.notifyUpdate()

                showProgressState.update {
                    it?.copy(
                        aired = progress?.progress?.aired ?: 0,
                        plays = progress?.progress?.plays ?: 0,
                    )
                }

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_removed)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingProgress.update { DONE }
            }
        }
    }

    private fun addToWatchlist() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingLists.update { LOADING }

                updateShowWatchlistUseCase.addToWatchlist(showId)
                userWatchlistLocalSource.addShows(
                    shows = listOf(
                        WatchlistItem.ShowItem(
                            rank = 0,
                            show = showState.value!!,
                            listedAt = nowUtcInstant(),
                        ),
                    ),
                    notify = true,
                )

                showProgressState.update {
                    it?.copy(
                        inWatchlist = true,
                    )
                }
                infoState.update {
                    DynamicStringResource(R.string.text_info_watchlist_added)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingLists.update { DONE }
            }
        }
    }

    private fun removeFromWatchlist() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingLists.update { LOADING }

                updateShowWatchlistUseCase.removeFromWatchlist(showId)
                userWatchlistLocalSource.removeShows(
                    ids = setOf(showId),
                    notify = true,
                )

                refreshLists()
                infoState.update {
                    DynamicStringResource(R.string.text_info_watchlist_removed)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingLists.update { DONE }
            }
        }
    }

    private fun addToList(listId: TraktId) {
        viewModelScope.launch {
            val show = showState.value
            if (!sessionManager.isAuthenticated() || show == null) {
                return@launch
            }
            try {
                loadingLists.update { LOADING }

                addListItemUseCase.addShow(
                    listId = listId,
                    show = show,
                )

                showProgressState.update {
                    it?.copy(inLists = true)
                }
                infoState.update {
                    DynamicStringResource(R.string.text_info_list_added)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingLists.update { DONE }
            }
        }
    }

    private fun removeFromList(listId: TraktId) {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingLists.update { LOADING }

                removeListItemUseCase.removeShow(
                    listId = listId,
                    showId = showId,
                )

                refreshLists()
                infoState.update {
                    DynamicStringResource(R.string.text_info_list_removed)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingLists.update { DONE }
            }
        }
    }

    private suspend fun refreshLists() {
        return coroutineScope {
            val watchlistAsync = async {
                loadWatchlistUseCase.loadLocalShows()
                    .firstOrNull {
                        it.show.ids.trakt == showId
                    }
            }

            val listsAsync = async {
                loadListsUseCase.loadLocalLists()
                    .values
                    .flatten()
                    .firstOrNull {
                        it.type == SHOW && it.id == showId
                    }
            }

            val watchlist = watchlistAsync.await()
            val lists = listsAsync.await()

            showProgressState.update {
                it?.copy(
                    inWatchlist = watchlist != null,
                    inLists = lists != null,
                )
            }
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ShowDetailsState> = combine(
        showState,
        showRatingsState,
        showStudiosState,
        showProgressState,
        loadingState,
        loadingProgress,
        loadingLists,
        infoState,
        errorState,
        userState,
    ) { state ->
        ShowDetailsState(
            show = state[0] as Show?,
            showRatings = state[1] as ExternalRating?,
            showStudios = state[2] as ImmutableList<String>?,
            showProgress = state[3] as ShowDetailsState.ProgressState?,
            loading = state[4] as LoadingState,
            loadingProgress = state[5] as LoadingState,
            loadingLists = state[6] as LoadingState,
            info = state[7] as StringResource?,
            error = state[8] as Exception?,
            user = state[9] as User?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
