package tv.trakt.trakt.core.home.sections.activity.features.all.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.home.HomeConfig.HOME_ALL_ACTIVITY_LIMIT
import tv.trakt.trakt.core.home.sections.activity.features.all.AllActivityState
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.usecases.GetSocialActivityUseCase
import java.time.LocalDate

internal class AllActivitySocialViewModel(
    private val getActivityUseCase: GetSocialActivityUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val sessionManager: SessionManager,
    analytics: Analytics,
    filterManager: GlobalFilterManager,
) : ViewModel() {
    private val initialState = AllActivityState()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val itemsFilterState = MutableStateFlow(filterManager.getFilter())
    private val usersFilterState = MutableStateFlow(initialState.usersFilter)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(Idle)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadUser()
        loadInitialData()

        analytics.logScreenView(
            screenName = "all_activity_social",
        )
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadInitialData() {
        dataJob = viewModelScope.launch {
            if (loadEmptyIfNeeded()) return@launch

            try {
                val localItems = getActivityUseCase.getLocalSocialActivity(
                    limit = HOME_ALL_ACTIVITY_LIMIT,
                    filter = itemsFilterState.value.mode,
                ).also {
                    loadUsersFilter(it)
                }

                itemsState
                    .update {
                        val selectedUser = usersFilterState.value.selectedUser
                        localItems
                            .filter { items -> selectedUser?.let { items.user == it } ?: true }
                            .groupBy { it.activityAt.toLocalDay() }
                            .mapValues { it.value.toImmutableList() }
                            .toImmutableMap()
                    }.also {
                        if (localItems.isEmpty()) {
                            loadingState.update { Loading }
                        }
                    }

                val remoteItems = getActivityUseCase.getSocialActivity(
                    page = 1,
                    limit = HOME_ALL_ACTIVITY_LIMIT,
                    filters = itemsFilterState.value,
                    skipLocal = true,
                )

                loadUsersFilter(remoteItems)
                itemsState
                    .update {
                        remoteItems
                            .groupBy { it.activityAt.toLocalDay() }
                            .mapValues { it.value.toImmutableList() }
                            .toImmutableMap()
                    }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                dataJob = null
            }
        }
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        if (processingJob?.isActive == true) {
            return
        }

        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val remoteItems = getActivityUseCase.getSocialActivity(
                    page = 1,
                    limit = HOME_ALL_ACTIVITY_LIMIT,
                    filters = itemsFilterState.value,
                    skipLocal = true,
                ).also {
                    loadUsersFilter(it)
                }

                itemsState
                    .update {
                        val selectedUser = usersFilterState.value.selectedUser
                        remoteItems
                            .filter { items -> selectedUser?.let { items.user == it } ?: true }
                            .groupBy { it.activityAt.toLocalDay() }
                            .mapValues { it.value.toImmutableList() }
                            .toImmutableMap()
                    }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                dataJob = null
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
        loadData()
    }

    fun setFilter(newFilter: GlobalFilter) {
        if (newFilter == itemsFilterState.value) {
            return
        }
        itemsFilterState.update { newFilter }
        loadData()
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update { persistentMapOf() }
            loadingState.update { Done }
            return true
        } else {
            itemsState.update { null }
            loadingState.update { Idle }
        }

        return false
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
                    Timber.recordError(error)
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
    val state = combine(
        itemsState,
        itemsFilterState,
        usersFilterState,
        navigateShow,
        navigateEpisode,
        navigateMovie,
        loadingState,
        loadingMoreState,
        userState,
        errorState,
    ) { state ->
        AllActivityState(
            items = state[0] as ImmutableMap<LocalDate, ImmutableList<HomeActivityItem>>?,
            itemsFilter = state[1] as GlobalFilter?,
            usersFilter = state[2] as AllActivityState.UsersFilter,
            navigateShow = state[3] as TraktId?,
            navigateEpisode = state[4] as Pair<TraktId, Episode>?,
            navigateMovie = state[5] as TraktId?,
            loading = state[6] as LoadingState,
            loadingMore = state[7] as LoadingState,
            user = state[8] as User?,
            error = state[9] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
