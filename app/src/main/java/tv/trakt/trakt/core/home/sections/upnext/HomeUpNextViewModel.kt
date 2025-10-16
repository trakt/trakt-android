package tv.trakt.trakt.core.home.sections.upnext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StaticStringResource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.HomeUpNextState.ItemsState
import tv.trakt.trakt.core.home.sections.upnext.features.all.data.local.AllUpNextLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow
import tv.trakt.trakt.core.home.sections.upnext.usecases.GetUpNextUseCase
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.PROGRESS
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.SEASON
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.user.usecase.progress.LoadUserProgressUseCase

@OptIn(FlowPreview::class)
internal class HomeUpNextViewModel(
    private val getUpNextUseCase: GetUpNextUseCase,
    private val updateHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val allUpNextSource: AllUpNextLocalDataSource,
    private val homePersonalActivitySource: HomePersonalLocalDataSource,
    private val showUpdatesSource: ShowDetailsUpdates,
    private val episodeUpdatesSource: EpisodeDetailsUpdates,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = HomeUpNextState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var user: User? = null
    private var itemsOrder: List<Int>? = null
    private var processingJob: Job? = null

    init {
        loadData()
        observeUser()
        observeData()
    }

    private fun observeUser() {
        viewModelScope.launch {
            user = sessionManager.getProfile()
            sessionManager.observeProfile()
                .drop(1)
                .distinctUntilChanged()
                .debounce(200)
                .collect {
                    user = it
                    loadData()
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeData() {
        merge(
            homePersonalActivitySource.observeUpdates(),
            showUpdatesSource.observeUpdates(Source.PROGRESS),
            showUpdatesSource.observeUpdates(Source.SEASONS),
            episodeUpdatesSource.observeUpdates(PROGRESS),
            episodeUpdatesSource.observeUpdates(SEASON),
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(
                    ignoreErrors = true,
                )
            }.launchIn(viewModelScope)

        allUpNextSource.observeUpdates()
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(
                    ignoreErrors = true,
                    localOnly = true,
                )
            }.launchIn(viewModelScope)
    }

    fun loadData(
        resetScroll: Boolean = true,
        ignoreErrors: Boolean = false,
        localOnly: Boolean = false,
    ) {
        viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                val localItems = getUpNextUseCase.getLocalUpNext(
                    limit = HOME_SECTION_LIMIT,
                )
                if (localItems.isNotEmpty()) {
                    itemsState.update {
                        ItemsState(
                            items = localItems,
                            resetScroll = false,
                        )
                    }
                    loadingState.update { DONE }

                    if (localOnly) {
                        itemsState.update { it.copy(resetScroll = resetScroll) }
                        return@launch
                    }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    ItemsState(
                        items = getUpNextUseCase.getUpNext(
                            page = 1,
                            limit = HOME_SECTION_LIMIT,
                            notify = false,
                        ),
                        resetScroll = resetScroll,
                    )
                }

                itemsOrder = itemsState.value.items?.map { it.show.ids.trakt.value }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.d(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                ItemsState(emptyList<ProgressShow>().toImmutableList())
            }
            loadingState.update { DONE }
            return true
        } else {
//            itemsState.update { ItemsState(resetScroll = false) }
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
                val currentItems = itemsState.value.items?.toMutableList() ?: return@launch

                val itemIndex = currentItems.indexOfFirst { it.id == episodeId }
                val itemLoading = currentItems[itemIndex].copy(loading = true)
                currentItems[itemIndex] = itemLoading

                itemsState.update {
                    ItemsState(
                        items = currentItems.toImmutableList(),
                        resetScroll = false,
                    )
                }

                updateHistoryUseCase.addToHistory(episodeId)

                // TODO Temporarily delay to mitigation DB replication
                delay(100)

                itemsState.update {
                    val items = getUpNextUseCase.getUpNext(
                        page = 1,
                        limit = HOME_SECTION_LIMIT,
                        notify = true,
                    )
                    ItemsState(
                        items = itemsOrder?.let { order ->
                            items
                                .sortedBy {
                                    order.indexOf(it.show.ids.trakt.value).run {
                                        // Items not in the list are placed at the end.
                                        if (this < 0) Int.MAX_VALUE else this
                                    }
                                }
                                .toImmutableList()
                        } ?: items,
                        resetScroll = false,
                    )
                }
                loadUserProgress()

                infoState.update {
                    StaticStringResource("Added to history")
                }
                itemsOrder = itemsState.value.items?.map { it.show.ids.trakt.value }
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

    fun clearInfo() {
        infoState.update { null }
    }

    val state: StateFlow<HomeUpNextState> = combine(
        loadingState,
        itemsState,
        infoState,
        errorState,
    ) { s1, s2, s3, s4 ->
        HomeUpNextState(
            loading = s1,
            items = s2,
            info = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
