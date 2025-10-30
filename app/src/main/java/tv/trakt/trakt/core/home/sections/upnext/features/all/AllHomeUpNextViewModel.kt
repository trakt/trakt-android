package tv.trakt.trakt.core.home.sections.upnext.features.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.HomeConfig.HOME_ALL_LIMIT
import tv.trakt.trakt.core.home.sections.upnext.features.all.data.local.AllUpNextLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow
import tv.trakt.trakt.core.home.sections.upnext.usecases.GetUpNextUseCase
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.PROGRESS
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.SEASON
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.resources.R

@OptIn(FlowPreview::class)
internal class AllHomeUpNextViewModel(
    private val getUpNextUseCase: GetUpNextUseCase,
    private val updateHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val allUpNextSource: AllUpNextLocalDataSource,
    private val showUpdatesSource: ShowDetailsUpdates,
    private val episodeUpdatesSource: EpisodeDetailsUpdates,
    private val movieDetailsUpdates: MovieDetailsUpdates,
    private val sessionManager: SessionManager,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = AllHomeUpNextState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(IDLE)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var itemsOrder: List<Int>? = null
    private var processingJob: Job? = null

    private var pages: Int = 1
    private var hasMoreData: Boolean = true

    init {
        loadBackground()
        loadData()
        observeData()

        analytics.logScreenView(
            screenName = "AllUpNext",
        )
    }

    private fun observeData() {
        merge(
            showUpdatesSource.observeUpdates(Source.PROGRESS),
            showUpdatesSource.observeUpdates(Source.SEASONS),
            episodeUpdatesSource.observeUpdates(PROGRESS),
            episodeUpdatesSource.observeUpdates(SEASON),
            movieDetailsUpdates.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(ignoreErrors = true)
            }.launchIn(viewModelScope)
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
                val localItems = getUpNextUseCase.getLocalUpNext(
                    limit = HOME_ALL_LIMIT,
                )
                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                val remoteItems = getUpNextUseCase.getUpNext(
                    page = 1,
                    limit = HOME_ALL_LIMIT,
                    notify = false,
                )
                itemsState.update { remoteItems }

                itemsOrder = itemsState.value?.map { it.show.ids.trakt.value }
                hasMoreData = remoteItems.size >= HOME_ALL_LIMIT
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

    fun loadMoreData() {
        if (itemsState.value.isNullOrEmpty() || !hasMoreData) {
            return
        }

        if (loadingMoreState.value.isLoading || loadingState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingMoreState.update { LOADING }

                val nextData = getUpNextUseCase.getUpNext(
                    page = pages + 1,
                    limit = HOME_ALL_LIMIT,
                    notify = false,
                )

                itemsState.update { items ->
                    items
                        ?.plus(nextData)
                        ?.distinctBy { it.key }
                        ?.toImmutableList()
                }

                pages += 1
                itemsOrder = itemsState.value?.map { it.show.ids.trakt.value }
                hasMoreData = nextData.size >= HOME_ALL_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error, "Failed to load more page data")
                }
            } finally {
                loadingMoreState.update { DONE }
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<ProgressShow>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        } else {
            itemsState.update { null }
            loadingState.update { IDLE }
        }

        return false
    }

    fun addToHistory(episodeId: TraktId) {
        if (processingJob != null) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                val currentItems = itemsState.value?.toMutableList() ?: return@launch

                val itemIndex = currentItems.indexOfFirst { it.id == episodeId }
                val itemLoading = currentItems[itemIndex].copy(loading = true)
                currentItems[itemIndex] = itemLoading

                itemsState.update {
                    currentItems.toImmutableList()
                }

                updateHistoryUseCase.addToHistory(episodeId)

                itemsState.update {
                    val items = getUpNextUseCase.getUpNext(
                        page = 1,
                        limit = HOME_ALL_LIMIT,
                        notify = true,
                    )
                    itemsOrder?.let { order ->
                        items
                            .sortedBy {
                                order.indexOf(it.show.ids.trakt.value)
                            }
                            .toImmutableList()
                    } ?: items
                }

                allUpNextSource.notifyUpdate()
                loadUserProgress()

                infoState.update { DynamicStringResource(R.string.text_info_history_added) }
                itemsOrder = itemsState.value?.map { it.show.ids.trakt.value }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.d(error, "Failed to add movie to history")
                }
            } finally {
                processingJob = null
            }
        }
    }

    fun loadUserProgress() {
        viewModelScope.launch {
            try {
                loadUserProgressUseCase.loadShowsProgress()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    fun removeShow(showId: TraktId) {
        itemsState.update { items ->
            items
                ?.filter { it.show.ids.trakt != showId }
                ?.toImmutableList()
        }
        itemsOrder = itemsState.value?.map {
            it.show.ids.trakt.value
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    private fun clear() {
        pages = 1
        hasMoreData = true
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<AllHomeUpNextState> = combine(
        backgroundState,
        itemsState,
        loadingState,
        loadingMoreState,
        infoState,
        errorState,
    ) { state ->
        AllHomeUpNextState(
            backgroundUrl = state[0] as String,
            items = state[1] as ImmutableList<ProgressShow>?,
            loading = state[2] as LoadingState,
            loadingMore = state[3] as LoadingState,
            info = state[4] as StringResource?,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
