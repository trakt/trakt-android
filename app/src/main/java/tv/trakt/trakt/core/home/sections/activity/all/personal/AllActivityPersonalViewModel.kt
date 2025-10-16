package tv.trakt.trakt.core.home.sections.activity.all.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.HomeConfig.HOME_ALL_LIMIT
import tv.trakt.trakt.core.home.sections.activity.all.AllActivityState
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.usecases.GetPersonalActivityUseCase

internal class AllActivityPersonalViewModel(
    private val getActivityUseCase: GetPersonalActivityUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = AllActivityState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val itemsState = MutableStateFlow(initialState.items)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(IDLE)
    private val errorState = MutableStateFlow(initialState.error)

    private var pages: Int = 1
    private var hasMoreData: Boolean = false
    private var processingJob: kotlinx.coroutines.Job? = null

    init {
        loadBackground()
        loadData()
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
                val localItems = getActivityUseCase.getLocalPersonalActivity(
                    limit = HOME_ALL_LIMIT,
                )
                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                val remoteItems = getActivityUseCase.getPersonalActivity(
                    page = 1,
                    limit = HOME_ALL_LIMIT,
                )
                itemsState.update { remoteItems }

                hasMoreData = remoteItems.size >= HOME_ALL_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.w(error, "Error loading social activity")
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

                val nextData = getActivityUseCase.getPersonalActivity(
                    page = pages + 1,
                    limit = HOME_ALL_LIMIT,
                )

                itemsState.update { items ->
                    items
                        ?.plus(nextData)
                        ?.distinctBy { it.id }
                        ?.toImmutableList()
                }

                pages += 1
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

    fun removeItem(item: HomeActivityItem) {
        itemsState.update {
            it?.filterNot { existingItem -> existingItem.id == item.id }
                ?.toImmutableList()
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

    fun navigateToEpisode(
        show: Show,
        episode: Episode,
    ) {
        if (navigateEpisode.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            episodeLocalDataSource.upsertEpisodes(listOf(episode))

            navigateEpisode.update {
                Pair(show.ids.trakt, episode)
            }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                movieLocalDataSource.upsertMovies(listOf(movie))
                navigateMovie.update { movie.ids.trakt }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error, "Failed to navigate to movie")
                }
            } finally {
                processingJob = null
            }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateEpisode.update { null }
        navigateMovie.update { null }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<HomeActivityItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        } else {
            itemsState.update { null }
            loadingState.update { IDLE }
        }

        return false
    }

    private fun clear() {
        pages = 1
        hasMoreData = true
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<AllActivityState> = combine(
        backgroundState,
        itemsState,
        navigateShow,
        navigateEpisode,
        navigateMovie,
        loadingState,
        loadingMoreState,
        errorState,
    ) { state ->
        AllActivityState(
            backgroundUrl = state[0] as String,
            items = state[1] as ImmutableList<HomeActivityItem>?,
            navigateShow = state[2] as TraktId?,
            navigateEpisode = state[3] as Pair<TraktId, Episode>?,
            navigateMovie = state[4] as TraktId?,
            loading = state[5] as LoadingState,
            loadingMore = state[6] as LoadingState,
            error = state[7] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
