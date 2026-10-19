@file:OptIn(FlowPreview::class)

package tv.trakt.trakt.core.today.pill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.observeForegroundRefresh
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalLocalDataSource
import tv.trakt.trakt.core.today.data.local.TodaySeenLocalDataSource
import tv.trakt.trakt.core.today.model.TodayStoryGroup
import tv.trakt.trakt.core.today.usecases.GetTodayStoriesUseCase
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

@Suppress("UNCHECKED_CAST")
internal class TodayPillViewModel(
    private val seenSource: TodaySeenLocalDataSource,
    private val getTodayStoriesUseCase: GetTodayStoriesUseCase,
    private val sessionManager: SessionManager,
    private val dataChanges: DataChanges,
    private val homePersonalActivitySource: HomePersonalLocalDataSource,
    private val appLifecycleProvider: AppLifecycleProvider,
) : ViewModel() {
    private val initialState = TodayPillState()

    private val groupsState = MutableStateFlow(initialState.groups)
    private val loadingState = MutableStateFlow(initialState.loading)

    private var dataJob: Job? = null

    init {
        loadData()
        observeUser()
        observeTodayUpdates()
        observeHistoryUpdates()
        observeForeground()
    }

    private fun observeForeground() {
        appLifecycleProvider.observeForegroundRefresh()
            .onEach { loadData() }
            .launchIn(viewModelScope)
    }

    private fun observeHistoryUpdates() {
        viewModelScope.launch {
            merge(
                dataChanges.observe(Kind.Watchlist),
                dataChanges.observe(
                    Kind.History,
                    Kind.CheckIn,
                    exclude = ChangeOrigin.TodayStories,
                ),
                homePersonalActivitySource.observeUpdates(),
            )
                .debounce(200.milliseconds)
                .collectLatest { refreshForYou() }
        }
    }

    private suspend fun refreshForYou() {
        if (!sessionManager.isAuthenticated()) return

        try {
            groupsState.update {
                getTodayStoriesUseCase.refreshForYou(MediaMode.Media)
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    private fun observeTodayUpdates() {
        viewModelScope.launch {
            dataChanges.observe(Kind.History)
                .filter { it.origin == ChangeOrigin.TodayStories }
                .debounce(200.milliseconds)
                .collect {
                    groupsState.update {
                        getTodayStoriesUseCase.getLocalStories(MediaMode.Media)
                    }
                }
        }
    }

    private fun observeUser() {
        viewModelScope.launch {
            sessionManager.observeProfile()
                .drop(1)
                .distinctUntilChanged()
                .debounce(200.milliseconds)
                .collect { loadData() }
        }
    }

    fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    groupsState.update { EmptyImmutableList }
                    return@launch
                }

                val localGroups = getTodayStoriesUseCase.getLocalStories(MediaMode.Media)
                if (localGroups.isNotEmpty()) {
                    groupsState.update { localGroups }
                } else {
                    loadingState.update { Loading }
                }

                groupsState.update {
                    getTodayStoriesUseCase.getStories(MediaMode.Media)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                dataJob = null
            }
        }
    }

    fun markAllSeen() {
        val keys = groupsState.value.map { it.key }
        if (keys.isEmpty()) return

        viewModelScope.launch {
            try {
                seenSource.markSeen(keys = keys, seenAt = nowUtcInstant())
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    val state = combine(
        groupsState,
        seenSource.observeSeen(),
        loadingState,
    ) { state ->
        TodayPillState(
            groups = state[0] as ImmutableList<TodayStoryGroup>,
            seenAt = (state[1] as Map<String, Instant>).toImmutableMap(),
            loading = state[2] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
