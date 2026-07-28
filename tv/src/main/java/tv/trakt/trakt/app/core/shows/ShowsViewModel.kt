package tv.trakt.trakt.app.core.shows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.app.core.shows.model.AnticipatedShow
import tv.trakt.trakt.app.core.shows.model.TrendingShow
import tv.trakt.trakt.app.core.shows.usecase.GetAnticipatedShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetPopularShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetReleasesShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetTrendingShowsUseCase
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State.FOREGROUND
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import java.time.ZonedDateTime

internal class ShowsViewModel(
    private val getTrendingShowsUseCase: GetTrendingShowsUseCase,
    private val getPopularShowsUseCase: GetPopularShowsUseCase,
    private val getAnticipatedShowsUseCase: GetAnticipatedShowsUseCase,
    private val getReleasesShowsUseCase: GetReleasesShowsUseCase,
    private val sessionManager: SessionManager,
    private val appLifecycleProvider: AppLifecycleProvider,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val initialState = ShowsState()

    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val trendingShowsState = MutableStateFlow(initialState.trendingShows)
    private val popularShowsState = MutableStateFlow(initialState.popularShows)
    private val anticipatedShowsState = MutableStateFlow(initialState.anticipatedShows)
    private val releasesShowsState = MutableStateFlow(initialState.releasesShows)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadedAt: ZonedDateTime? = null

    init {
        loadData()
        observeApp()
        observeData()
    }

    private fun observeData() {
        collectionStateProvider
            .launchIn(viewModelScope)
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

    private fun loadData(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingState.update { true }
                }

                userState.update {
                    sessionManager.getProfile()
                }

                coroutineScope {
                    val trendingShowsAsync = async { getTrendingShowsUseCase.getTrendingShows() }
                    val popularShowsAsync = async { getPopularShowsUseCase.getPopularShows() }
                    val anticipatedShowsAsync = async { getAnticipatedShowsUseCase.getAnticipatedShows() }
                    val releasesShowsAsync = async { getReleasesShowsUseCase.getReleases() }

                    val trendingShows = trendingShowsAsync.await()
                    val popularShows = popularShowsAsync.await()
                    val anticipatedShows = anticipatedShowsAsync.await()
                    val releasesShows = releasesShowsAsync.await()

                    trendingShowsState.value = trendingShows
                    popularShowsState.value = popularShows
                    anticipatedShowsState.value = anticipatedShows
                    releasesShowsState.value = releasesShows
                }

                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, error.toString())
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        loadingState,
        trendingShowsState,
        popularShowsState,
        anticipatedShowsState,
        releasesShowsState,
        userState,
        collectionStateProvider.stateFlow,
        errorState,
    ) { state ->
        ShowsState(
            isLoading = state[0] as Boolean,
            trendingShows = state[1] as ImmutableList<TrendingShow>?,
            popularShows = state[2] as ImmutableList<Show>?,
            anticipatedShows = state[3] as ImmutableList<AnticipatedShow>?,
            releasesShows = state[4] as ImmutableList<HomeUpcomingItem.EpisodeItem>?,
            user = state[5] as User?,
            collection = state[6] as UserCollectionState,
            error = state[7] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
