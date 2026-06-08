@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.home.sections.upnext

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.inappreview.RequestAppReviewUseCase
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaMode.MOVIES
import tv.trakt.trakt.common.model.MediaMode.SHOWS
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates.Source.HomeUpNext
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.HomeUpNextState.ItemsState
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.features.all.data.local.UpNextUpdates
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextMovie
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextShow
import tv.trakt.trakt.core.home.sections.upnext.usecases.GetUpNextUseCase
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.CALENDAR
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.HOME
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.PROGRESS
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source.SEASON
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates.Source.AllWatchlist
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates.Source.Default
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import tv.trakt.trakt.resources.R

@OptIn(FlowPreview::class)
internal class HomeUpNextViewModel(
    private val appContext: Context,
    private val getUpNextUseCase: GetUpNextUseCase,
    private val updateHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val appReviewUseCase: RequestAppReviewUseCase,
    private val homeUpNextSource: HomeUpNextLocalDataSource,
    private val homePersonalActivitySource: HomePersonalLocalDataSource,
    private val watchlistUpdates: WatchlistUpdates,
    private val upNextUpdates: UpNextUpdates,
    private val showUpdates: ShowDetailsUpdates,
    private val episodeUpdates: EpisodeDetailsUpdates,
    private val checkInUpdates: CheckInUpdates,
    private val checkInManager: CheckInManager,
    private val filterManager: GlobalFilterManager,
    private val sessionManager: SessionManager,
    private val collapsingManager: CollapsingManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = HomeUpNextState()

    private val filterState = MutableStateFlow(filterManager.getFilter())
    private val collapseState = MutableStateFlow(isCollapsed())
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var user: User? = null
    private var itemsOrder: List<String>? = null
    private var dataJob: Job? = null
    private var processingJob: Job? = null
    private var collapseJob: Job? = null

    init {
        loadData()

        observeUser()
        observeData()
        observeMode()
    }

    private fun observeMode() {
        filterManager.observeFilter()
            .distinctUntilChanged()
            .onEach { value ->
                filterState.update { value }
                collapseState.update { isCollapsed() }
                loadData()
            }
            .launchIn(viewModelScope)
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
            watchlistUpdates.observeUpdates(Default),
            watchlistUpdates.observeUpdates(AllWatchlist),
            homePersonalActivitySource.observeUpdates(),
            showUpdates.observeUpdates(Source.PROGRESS),
            showUpdates.observeUpdates(Source.SEASONS),
            episodeUpdates.observeUpdates(PROGRESS),
            episodeUpdates.observeUpdates(SEASON),
            episodeUpdates.observeUpdates(HOME),
            episodeUpdates.observeUpdates(CALENDAR),
            checkInUpdates.observeUpdates().filterNot { it.first == HomeUpNext },
        )
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(ignoreErrors = true)
            }
            .launchIn(viewModelScope)

        upNextUpdates.observeUpdates()
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadData(ignoreErrors = true)
            }.launchIn(viewModelScope)
    }

    fun loadData(
        resetScroll: Boolean = true,
        ignoreErrors: Boolean = false,
        localOnly: Boolean = false,
    ) {
        if (processingJob?.isActive == true) return
        if (dataJob?.isActive == true) return

        dataJob = viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                val localItems = getUpNextUseCase.getLocalUpNext(
                    limit = HOME_SECTION_LIMIT,
                ).filter {
                    when (filterState.value.mode) {
                        SHOWS -> it is UpNextShow
                        MOVIES -> it is UpNextMovie
                        else -> true
                    }
                }.toImmutableList()

                if (localItems.isNotEmpty()) {
                    itemsState.update {
                        ItemsState(
                            items = localItems,
                            resetScroll = false,
                        )
                    }
                    loadingState.update { Done }

                    if (localOnly) {
                        itemsState.update { it.copy(resetScroll = resetScroll) }
                        return@launch
                    }
                } else {
                    loadingState.update { Loading }
                }

                itemsState.update {
                    ItemsState(
                        items = getUpNextUseCase.getUpNext(
                            page = 1,
                            limit = HOME_SECTION_LIMIT,
                            filters = filterState.value,
                        ).filter {
                            when (filterState.value.mode) {
                                SHOWS -> it is UpNextShow
                                MOVIES -> it is UpNextMovie
                                else -> true
                            }
                        }.toImmutableList(),
                        resetScroll = resetScroll,
                    )
                }

                itemsOrder = itemsState.value.items?.map { "${it.mediaId}-${it.type}" }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.d(error, "Failed to load data")
                }
            } finally {
                loadingState.update { Done }
                dataJob = null
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                ItemsState(emptyList<UpNextShow>().toImmutableList())
            }
            loadingState.update { Done }
            return true
        } else {
            loadingState.update { Idle }
        }

        return false
    }

    fun addToHistory(
        episodeId: TraktId,
        customDate: DateSelectionResult? = null,
    ) {
        if (processingJob != null) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                val currentItems = itemsState.value.items?.toMutableList() ?: return@launch

                val itemIndex = currentItems.indexOfFirst { it.type == SHOW && it.id == episodeId }
                val itemLoading = (currentItems[itemIndex] as UpNextShow).copy(loading = true)
                currentItems[itemIndex] = itemLoading

                itemsState.update {
                    ItemsState(
                        items = currentItems.toImmutableList(),
                        resetScroll = false,
                    )
                }

                updateHistoryUseCase.addToHistory(
                    episodeId = episodeId,
                    customDate = customDate,
                )

                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "home_up_next",
                    date = customDate?.analyticsStrings,
                )

                itemsState.update {
                    val items = getUpNextUseCase.getUpNext(
                        page = 1,
                        limit = HOME_SECTION_LIMIT,
                        filters = filterState.value,
                    )
                    ItemsState(
                        items = itemsOrder?.let { order ->
                            items
                                .sortedBy {
                                    order.indexOf("${it.mediaId}-${it.type}").run {
                                        // Items not in the list are placed at the end.
                                        if (this < 0) Int.MAX_VALUE else this
                                    }
                                }
                                .toImmutableList()
                        } ?: items,
                        resetScroll = false,
                    )
                }

                homeUpNextSource.notifyUpdate()
                loadUserProgress()

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_added)
                }

                itemsOrder = itemsState.value.items?.map { "${it.mediaId}-${it.type}" }
                appReviewUseCase.incrementCount()
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

    fun addEpisodeCheckIn(
        showId: TraktId,
        episodeId: TraktId,
        seasonEpisode: SeasonEpisode,
    ) {
        if (processingJob?.isActive == true) {
            return
        }

        processingJob = viewModelScope.launch {
            try {
                val currentItems = itemsState.value.items?.toMutableList() ?: return@launch

                val itemIndex = currentItems.indexOfFirst { it.type == SHOW && it.id == episodeId }
                val itemLoading = (currentItems[itemIndex] as UpNextShow).copy(loading = true)
                currentItems[itemIndex] = itemLoading

                itemsState.update {
                    ItemsState(
                        items = currentItems.toImmutableList(),
                        resetScroll = false,
                    )
                }

                checkInManager.startEpisode(
                    context = appContext,
                    showId = showId,
                    seasonEpisode = seasonEpisode,
                    source = HomeUpNext,
                )

                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "home_up_next",
                    date = "checkin",
                )

                itemsState.update {
                    val items = getUpNextUseCase.getUpNext(
                        page = 1,
                        limit = HOME_SECTION_LIMIT,
                        filters = filterState.value,
                    )
                    ItemsState(
                        items = itemsOrder?.let { order ->
                            items
                                .sortedBy {
                                    order.indexOf("${it.mediaId}-${it.type}").run {
                                        // Items not in the list are placed at the end.
                                        if (this < 0) Int.MAX_VALUE else this
                                    }
                                }
                                .toImmutableList()
                        } ?: items,
                        resetScroll = false,
                    )
                }

                itemsOrder = itemsState.value.items?.map { "${it.mediaId}-${it.type}" }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
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
                    Timber.recordError(error)
                }
            }
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            val key = when (filterState.value.mode) {
                MediaMode.MEDIA -> CollapsingKey.HOME_MEDIA_UP_NEXT
                SHOWS -> CollapsingKey.HOME_SHOWS_UP_NEXT
                MOVIES -> CollapsingKey.HOME_MOVIES_UP_NEXT
            }
            when {
                collapsed -> collapsingManager.collapse(key)
                else -> collapsingManager.expand(key)
            }
        }
    }

    private fun isCollapsed(): Boolean {
        return collapsingManager.isCollapsed(
            key = when (filterState.value.mode) {
                MediaMode.MEDIA -> CollapsingKey.HOME_MEDIA_UP_NEXT
                SHOWS -> CollapsingKey.HOME_SHOWS_UP_NEXT
                MOVIES -> CollapsingKey.HOME_MOVIES_UP_NEXT
            },
        )
    }

    fun clearInfo() {
        infoState.update { null }
    }

    val state = combine(
        loadingState,
        collapseState,
        itemsState,
        infoState,
        errorState,
        filterState,
    ) { state ->
        HomeUpNextState(
            loading = state[0] as LoadingState,
            collapsed = state[1] as Boolean,
            items = state[2] as ItemsState,
            info = state[3] as StringResource?,
            error = state[4] as Exception?,
            filter = state[5] as GlobalFilter,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
