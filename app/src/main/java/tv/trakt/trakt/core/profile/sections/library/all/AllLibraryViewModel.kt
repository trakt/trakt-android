package tv.trakt.trakt.core.profile.sections.library.all

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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.library.model.LibraryFilter
import tv.trakt.trakt.core.library.model.LibraryItem
import tv.trakt.trakt.core.user.usecases.lists.LoadUserLibraryUseCase

@OptIn(FlowPreview::class)
internal class AllLibraryViewModel(
    private val loadLibraryUseCase: LoadUserLibraryUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val sessionManager: SessionManager,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = AllLibraryState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null

    init {
        loadData()

        analytics.logScreenView(
            screenName = "all_library",
        )
    }

    fun loadData(
        ignoreErrors: Boolean = false,
        localOnly: Boolean = false,
    ) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val localItems = loadLibraryUseCase.loadLocalAll()
                if (localItems.isNotEmpty()) {
                    val filter = filterState.value
                    itemsState.update {
                        localItems
                            .filter {
                                when (filterState.value) {
                                    null -> true
                                    LibraryFilter.CUSTOM -> it.availableOn.isEmpty()
                                    LibraryFilter.PLEX -> it.availableOn.contains(filter?.value)
                                }
                            }
                            .toImmutableList()
                    }
                    loadingState.update { DONE }
                    if (localOnly) {
                        return@launch
                    }
                } else {
                    loadingState.update { LOADING }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<LibraryItem>().toImmutableList()
            }
            loadingState.update { DONE }
            return true
        }

        return false
    }

    fun setFilter(newFilter: LibraryFilter) {
        if (loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            when (filterState.value) {
                newFilter -> filterState.update { null }
                else -> filterState.update { newFilter }
            }
            loadData(localOnly = true)
        }
    }

    suspend fun onNavigateToShow(show: Show) {
        showLocalDataSource.upsertShows(listOf(show))
    }

    suspend fun onNavigateToMovie(movie: Movie) {
        movieLocalDataSource.upsertMovies(listOf(movie))
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
    }

    override fun onCleared() {
        dataJob?.cancel()
        dataJob = null
        super.onCleared()
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<AllLibraryState> = combine(
        loadingState,
        itemsState,
        filterState,
        navigateShow,
        navigateMovie,
        errorState,
    ) { state ->
        AllLibraryState(
            loading = state[0] as LoadingState,
            items = state[1] as? ImmutableList<LibraryItem>,
            filter = state[2] as? LibraryFilter,
            navigateShow = state[3] as? TraktId,
            navigateMovie = state[4] as? TraktId,
            error = state[5] as? Exception,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
