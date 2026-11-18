package tv.trakt.trakt.app.core.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Dispatchers
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
import tv.trakt.trakt.app.core.discover.usecase.GetDiscoverSectionsUseCase
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.data.local.MediaModeLocalSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State.FOREGROUND
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.calendar.CalendarItem
import tv.trakt.trakt.common.model.discover.DiscoverItem
import java.time.ZonedDateTime

@Suppress("UNCHECKED_CAST")
internal class DiscoverViewModel(
    private val getSectionsUseCase: GetDiscoverSectionsUseCase,
    private val appLifecycleProvider: AppLifecycleProvider,
    private val collectionStateProvider: CollectionStateProvider,
    private val mediaModeLocalSource: MediaModeLocalSource,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = DiscoverState()

    private val modeState = MutableStateFlow(initialState.mode)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val trendingState = MutableStateFlow(initialState.trending)
    private val releasesState = MutableStateFlow(initialState.releases)
    private val anticipatedState = MutableStateFlow(initialState.anticipated)
    private val popularState = MutableStateFlow(initialState.popular)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadedAt: ZonedDateTime? = null
    private var dataJob: Job? = null

    init {
        observeMode()
        observeData()
        observeApp()

        analytics.logScreenView(
            screenName = "discover",
        )
    }

    private fun observeApp() {
        appLifecycleProvider.observeState(FOREGROUND)
            .filter {
                loadedAt != null &&
                    nowUtc().minusMinutes(REFRESH_DATA_THRESHOLD_MINUTES).isAfter(loadedAt)
            }
            .onEach {
                loadData(showLoading = false)
            }
            .launchIn(viewModelScope)
    }

    private fun observeMode() {
        mediaModeLocalSource.observeMode()
            .onEach { mode ->
                modeState.update { mode }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun observeData() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun loadData(showLoading: Boolean = true) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                if (showLoading) {
                    loadingState.update { true }
                    errorState.update { null }
                }

                val sections = getSectionsUseCase.getSections(modeState.value)
                trendingState.update { sections.trending }
                releasesState.update { sections.releases }
                anticipatedState.update { sections.anticipated }
                popularState.update { sections.popular }

                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, error.message ?: "Unknown error")
                }
            }

            loadingState.update { false }
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

    val state = combine(
        modeState,
        loadingState,
        trendingState,
        releasesState,
        anticipatedState,
        popularState,
        collectionStateProvider.stateFlow,
        errorState,
    ) { state ->
        DiscoverState(
            mode = state[0] as MediaMode,
            isLoading = state[1] as Boolean,
            trending = state[2] as ImmutableList<DiscoverItem>?,
            releases = state[3] as ImmutableList<CalendarItem>?,
            anticipated = state[4] as ImmutableList<DiscoverItem>?,
            popular = state[5] as ImmutableList<DiscoverItem>?,
            collection = state[6] as UserCollectionState,
            error = state[7] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
