@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.app.core.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.Config.REFRESH_DATA_THRESHOLD_MINUTES
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.app.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.app.core.lists.usecases.GetListsLikedUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsMediaWatchlistUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsMoviesWatchlistUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsPersonalUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsShowsWatchlistUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsSmartUseCase
import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalSource
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.data.local.MediaModeLocalSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State.FOREGROUND
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider
import java.time.ZonedDateTime

internal class ListsViewModel(
    private val getMediaWatchlistUseCase: GetListsMediaWatchlistUseCase,
    private val getShowsWatchlistUseCase: GetListsShowsWatchlistUseCase,
    private val getMoviesWatchlistUseCase: GetListsMoviesWatchlistUseCase,
    private val getSmartUseCase: GetListsSmartUseCase,
    private val getPersonalUseCase: GetListsPersonalUseCase,
    private val getLikedUseCase: GetListsLikedUseCase,
    private val showsLocalSyncSource: ShowsSyncLocalSource,
    private val moviesLocalSyncSource: MoviesSyncLocalSource,
    private val appLifecycleProvider: AppLifecycleProvider,
    private val cacheMarkerProvider: CacheMarkerProvider,
    private val collectionStateProvider: CollectionStateProvider,
    private val mediaModeLocalSource: MediaModeLocalSource,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ListsState()

    private val modeState = MutableStateFlow(initialState.mode)
    private val watchlistState = MutableStateFlow(initialState.watchlist)
    private val listsSmartState = MutableStateFlow(initialState.smartLists)
    private val listsPersonalState = MutableStateFlow(initialState.personalLists)
    private val listsLikedState = MutableStateFlow(initialState.likedLists)
    private val loadingState = MutableStateFlow(initialState.loadingLists)
    private val errorState = MutableStateFlow(initialState.error)

    private var watchlistLoadedAt: ZonedDateTime? = null
    private var watchlistJob: Job? = null
    private var marker: String? = null

    init {
        observeMode()
        loadSmartListsData()
        loadPersonalListsData()
        loadLikedListsData()

        observeApp()
        collectionStateProvider.launchIn(viewModelScope)
    }

    private fun observeMode() {
        mediaModeLocalSource.observeMode()
            .onEach { mode ->
                modeState.update { mode }
                loadWatchlistData()
            }
            .launchIn(viewModelScope)
    }

    private fun observeApp() {
        appLifecycleProvider.observeState(FOREGROUND)
            .filter {
                watchlistLoadedAt != null &&
                    nowUtc().minusMinutes(REFRESH_DATA_THRESHOLD_MINUTES).isAfter(watchlistLoadedAt)
            }
            .onEach {
                loadWatchlistData(showLoading = false)
            }
            .launchIn(viewModelScope)
    }

    private fun loadWatchlistData(showLoading: Boolean = true) {
        watchlistJob?.cancel()
        watchlistJob = viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingState.update {
                        it.copy(loadingWatchlist = true)
                    }
                }

                val items = when (modeState.value) {
                    MediaMode.Media -> getMediaWatchlistUseCase.getItems(
                        limit = LISTS_SECTION_LIMIT,
                        sorting = Sorting.RecentlyAdded,
                    )

                    MediaMode.Shows -> getShowsWatchlistUseCase.getShows(
                        limit = LISTS_SECTION_LIMIT,
                        sorting = Sorting.RecentlyAdded,
                    )

                    MediaMode.Movies -> getMoviesWatchlistUseCase.getMovies(
                        limit = LISTS_SECTION_LIMIT,
                        sorting = Sorting.RecentlyAdded,
                    )
                }

                watchlistState.update { items }
                watchlistLoadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Error loading: ${error.message}")
                }
            }

            loadingState.update {
                it.copy(loadingWatchlist = false)
            }
        }
    }

    private fun loadSmartListsData() {
        viewModelScope.launch {
            try {
                loadingState.update {
                    it.copy(loadingSmart = true)
                }
                val lists = getSmartUseCase.getLists()
                listsSmartState.update { lists }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Error loading smart lists")
                }
            } finally {
                loadingState.update {
                    it.copy(loadingSmart = false)
                }
            }
        }
    }

    private fun loadPersonalListsData() {
        Timber.d("Loading personal lists data")
        viewModelScope.launch {
            try {
                loadingState.update {
                    it.copy(loadingPersonal = true)
                }
                val lists = getPersonalUseCase.getLists()
                listsPersonalState.update { lists }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e("Error loading personal lists: ${error.message}")
                }
            } finally {
                loadingState.update {
                    it.copy(loadingPersonal = false)
                }
            }
        }
    }

    fun loadLikedListsData(reload: Boolean = false) {
        Timber.d("Loading liked lists data")
        viewModelScope.launch {
            try {
                loadingState.update {
                    it.copy(loadingLiked = !reload)
                }
                val lists = getLikedUseCase.getLists()
                listsLikedState.update { lists }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!reload) {
                        errorState.update { error }
                    }
                    Timber.e("Error loading liked lists: ${error.message}")
                }
            } finally {
                marker = cacheMarkerProvider.getMarker()
                loadingState.update {
                    it.copy(loadingLiked = false)
                }
            }
        }
    }

    fun setMode(mode: MediaMode) {
        if (mode == modeState.value) {
            return
        }

        modeState.update { mode }
        analytics.logMediaModeClick(mode = mode.name)
        viewModelScope.launch {
            mediaModeLocalSource.setMode(mode)
        }
    }

    fun updateWatchlistData() {
        Timber.d("updateWatchlistData() called")
        viewModelScope.launch {
            try {
                val loadedAt = watchlistLoadedAt ?: return@launch

                val showsUpdatedAt = showsLocalSyncSource.getWatchlistUpdatedAt()
                val moviesUpdatedAt = moviesLocalSyncSource.getWatchlistUpdatedAt()
                if (showsUpdatedAt?.isAfter(loadedAt) == true || moviesUpdatedAt?.isAfter(loadedAt) == true) {
                    loadWatchlistData(showLoading = false)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error, "Error")
                }
            }
        }
    }

    fun updateLikedListsData() {
        if (marker == null) return
        viewModelScope.launch {
            if (marker != cacheMarkerProvider.getMarker()) {
                loadLikedListsData(reload = true)
            }
        }
    }

    val state = combine(
        modeState,
        watchlistState,
        listsPersonalState,
        listsLikedState,
        loadingState,
        errorState,
        listsSmartState,
        collectionStateProvider.stateFlow,
    ) { s ->
        ListsState(
            mode = s[0] as MediaMode,
            watchlist = s[1] as ImmutableList<ListMediaItem>?,
            personalLists = s[2] as ImmutableList<CustomList>?,
            likedLists = s[3] as ImmutableList<CustomList>?,
            loadingLists = s[4] as ListsState.LoadingState,
            error = s[5] as Exception?,
            smartLists = s[6] as ImmutableList<SmartList>?,
            collection = s[7] as UserCollectionState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
