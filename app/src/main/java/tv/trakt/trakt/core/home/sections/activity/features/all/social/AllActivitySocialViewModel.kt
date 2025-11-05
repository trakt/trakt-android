package tv.trakt.trakt.core.home.sections.activity.features.all.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
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
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.HomeConfig.HOME_ALL_ACTIVITY_LIMIT
import tv.trakt.trakt.core.home.sections.activity.features.all.AllActivityState
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.usecases.GetSocialActivityUseCase

internal class AllActivitySocialViewModel(
    private val getActivityUseCase: GetSocialActivityUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val sessionManager: SessionManager,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = AllActivityState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val itemsState = MutableStateFlow(initialState.items)
    private val usersFilterState = MutableStateFlow(initialState.usersFilter)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(IDLE)
    private val errorState = MutableStateFlow(initialState.error)

    private var pages: Int = 1
    private var hasMoreData: Boolean = false
    private var processingJob: Job? = null

    init {
        loadBackground()
        loadData()

        analytics.logScreenView(
            screenName = "all_activity_social",
        )
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    private fun loadData(
        ignoreErrors: Boolean = false,
        localOnly: Boolean = false,
    ) {
        clear()
        viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                val localItems = getActivityUseCase.getLocalSocialActivity(
                    limit = HOME_ALL_ACTIVITY_LIMIT,
                )
                if (localItems.isNotEmpty()) {
                    loadUsersFilter(localItems)
                    itemsState.update {
                        val selectedUser = usersFilterState.value.selectedUser
                        localItems.filter { items ->
                            selectedUser?.let { items.user == it } ?: true
                        }.toImmutableList()
                    }
                    loadingState.update { DONE }

                    if (localOnly) {
                        return@launch
                    }
                } else {
                    loadingState.update { LOADING }
                }

                val remoteItems = getActivityUseCase.getSocialActivity(
                    page = 1,
                    limit = HOME_ALL_ACTIVITY_LIMIT,
                )
                loadUsersFilter(remoteItems)
                itemsState.update { remoteItems }

                hasMoreData = remoteItems.size >= HOME_ALL_ACTIVITY_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.e(error, "Error loading social activity")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private fun loadUsersFilter(items: List<HomeActivityItem>) {
        val users = items
            .groupBy { it.user }
            .map { (user, items) -> user to items.size }
            .sortedWith(
                compareByDescending<Pair<User?, Int>> { it.second }
                    .thenBy { it.first?.username?.lowercase() },
            )
            .mapNotNull { it.first }
            .take(10)

        if (users.size <= 1) {
            return
        }

        usersFilterState.update {
            AllActivityState.UsersFilter(
                users = it.users.plus(users).toImmutableSet(),
                selectedUser = it.selectedUser,
            )
        }
    }

    fun setUserFilter(user: User) {
        val currentFilter = usersFilterState.value
        val newFilter = when (currentFilter.selectedUser) {
            user -> currentFilter.copy(selectedUser = null)
            else -> currentFilter.copy(selectedUser = user)
        }

        usersFilterState.update { newFilter }
        loadData(localOnly = true)
    }

//    fun loadMoreData() {
//        if (itemsState.value.isNullOrEmpty() || !hasMoreData) {
//            return
//        }
//
//        if (loadingMoreState.value.isLoading || loadingState.value.isLoading) {
//            return
//        }
//
//        viewModelScope.launch {
//            try {
//                loadingMoreState.update { LOADING }
//
//                val nextData = getActivityUseCase.getSocialActivity(
//                    page = pages + 1,
//                    limit = HOME_ALL_ACTIVITY_LIMIT,
//                )
//
//                itemsState.update { items ->
//                    items
//                        ?.plus(nextData)
//                        ?.distinctBy { it.id }
//                        ?.toImmutableList()
//                }
//
//                pages += 1
//                hasMoreData = nextData.size >= HOME_ALL_ACTIVITY_LIMIT
//            } catch (error: Exception) {
//                error.rethrowCancellation {
//                    errorState.update { error }
//                    Timber.e(error, "Failed to load more page data")
//                }
//            } finally {
//                loadingMoreState.update { DONE }
//            }
//        }
//    }

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
                    Timber.e(error, "Failed to navigate to movie")
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

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<AllActivityState> = combine(
        backgroundState,
        itemsState,
        usersFilterState,
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
            usersFilter = state[2] as AllActivityState.UsersFilter,
            navigateShow = state[3] as TraktId?,
            navigateEpisode = state[4] as Pair<TraktId, Episode>?,
            navigateMovie = state[5] as TraktId?,
            loading = state[6] as LoadingState,
            loadingMore = state[7] as LoadingState,
            error = state[8] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
