@file:OptIn(FlowPreview::class)

package tv.trakt.trakt.core.home.sections.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.core.home.sections.activity.all.data.local.AllActivityLocalDataSource
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter.PERSONAL
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter.SOCIAL
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.usecases.GetActivityFilterUseCase
import tv.trakt.trakt.core.home.sections.activity.usecases.GetPersonalActivityUseCase
import tv.trakt.trakt.core.home.sections.activity.usecases.GetSocialActivityUseCase
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource

internal class HomeActivityViewModel(
    private val getActivityFilterUseCase: GetActivityFilterUseCase,
    private val getSocialActivityUseCase: GetSocialActivityUseCase,
    private val getPersonalActivityUseCase: GetPersonalActivityUseCase,
    private val homeUpNextSource: HomeUpNextLocalDataSource,
    private val userWatchlistSource: UserWatchlistLocalDataSource,
    private val allActivitySource: AllActivityLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = HomeActivityState()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadDataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadData()
        observeUser()
        observeHome()
    }

    private fun observeUser() {
        viewModelScope.launch {
            userState.update { sessionManager.getProfile() }
            sessionManager.observeProfile()
                .drop(1)
                .distinctUntilChanged()
                .debounce(250)
                .collect { user ->
                    userState.update { user }
                    loadData()
                }
        }
    }

    private fun observeHome() {
        merge(
            homeUpNextSource.observeUpdates(),
            userWatchlistSource.observeUpdates(),
            allActivitySource.observeUpdates(),
        )
            .distinctUntilChanged()
            .debounce(250)
            .onEach {
                loadData(ignoreErrors = true)
            }.launchIn(viewModelScope)
    }

    fun loadData(ignoreErrors: Boolean = false) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val filter = loadFilter()
                val localItems = when (filter) {
                    SOCIAL -> getSocialActivityUseCase.getLocalSocialActivity(
                        limit = HOME_SECTION_LIMIT,
                    )
                    PERSONAL -> getPersonalActivityUseCase.getLocalPersonalActivity(
                        limit = HOME_SECTION_LIMIT,
                    )
                }

                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    when (filter) {
                        SOCIAL -> getSocialActivityUseCase.getSocialActivity(1, HOME_SECTION_LIMIT)
                        PERSONAL -> getPersonalActivityUseCase.getPersonalActivity(1, HOME_SECTION_LIMIT)
                    }
                }
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

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<HomeActivityItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        }

        return false
    }

    private suspend fun loadFilter(): HomeActivityFilter {
        val filter = getActivityFilterUseCase.getFilter()
        filterState.update { filter }
        return filter
    }

    fun setFilter(newFilter: HomeActivityFilter) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            getActivityFilterUseCase.setFilter(newFilter)
            loadFilter()
            loadData()
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
        navigateMovie.update { null }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<HomeActivityState> = combine(
        loadingState,
        itemsState,
        filterState,
        navigateMovie,
        errorState,
        userState,
    ) { states ->
        HomeActivityState(
            loading = states[0] as LoadingState,
            items = states[1] as? ImmutableList<HomeActivityItem>,
            filter = states[2] as? HomeActivityFilter,
            navigateMovie = states[3] as? TraktId,
            error = states[4] as? Exception,
            user = states[5] as? User,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
