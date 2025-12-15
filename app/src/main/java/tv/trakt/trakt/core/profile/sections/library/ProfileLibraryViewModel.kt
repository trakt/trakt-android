@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.profile.sections.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.library.model.LibraryFilter
import tv.trakt.trakt.core.library.model.LibraryItem
import tv.trakt.trakt.core.lists.ListsConfig.LIBRARY_SECTION_LIMIT
import tv.trakt.trakt.core.user.usecases.lists.LoadUserLibraryUseCase

@OptIn(FlowPreview::class)
internal class ProfileLibraryViewModel(
    private val loadLibraryUseCase: LoadUserLibraryUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = ProfileLibraryState()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)

    private val filterState = MutableStateFlow(initialState.filter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadDataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadData()
//        observeData()
    }

//    private fun observeData() {
//        merge(
//            favoritesUpdates.observeUpdates(DETAILS),
//            favoritesUpdates.observeUpdates(CONTEXT_SHEET),
//        )
//            .distinctUntilChanged()
//            .debounce(200)
//            .onEach {
//                loadData(
//                    ignoreErrors = true,
//                    localOnly = true,
//                )
//            }
//            .launchIn(viewModelScope)
//    }

    fun loadData() {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val filter = filterState.value

                val episodesLoadedAsync = async { loadLibraryUseCase.isEpisodesLoaded() }
                val moviesLoadedAsync = async { loadLibraryUseCase.isMoviesLoaded() }
                val isLoaded = episodesLoadedAsync.await() && moviesLoadedAsync.await()

                val localItems = when {
                    isLoaded -> loadLibraryUseCase.loadLocalAll()
                    else -> EmptyImmutableList
                }

                if (localItems.isNotEmpty()) {
                    itemsState.update {
                        localItems
                            .filter {
                                when (filter) {
                                    null -> true
                                    LibraryFilter.CUSTOM -> it.availableOn.isEmpty()
                                    LibraryFilter.PLEX -> it.availableOn.contains(filter.value)
                                }
                            }
                            .take(LIBRARY_SECTION_LIMIT)
                            .toImmutableList()
                    }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    loadLibraryUseCase.loadAll()
                        .filter {
                            when (filter) {
                                null -> true
                                LibraryFilter.CUSTOM -> it.availableOn.isEmpty()
                                LibraryFilter.PLEX -> it.availableOn.contains(filter.value)
                            }
                        }
                        .take(LIBRARY_SECTION_LIMIT)
                        .toImmutableList()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { DONE }
                loadDataJob = null
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
        when (filterState.value) {
            newFilter -> filterState.update { null }
            else -> filterState.update { newFilter }
        }
        loadData()
    }

    suspend fun onNavigateToShow(show: Show) {
        showLocalDataSource.upsertShows(listOf(show))
    }

    suspend fun onNavigateToMovie(movie: Movie) {
        movieLocalDataSource.upsertMovies(listOf(movie))
    }

    val state = combine(
        loadingState,
        itemsState,
        filterState,
        errorState,
        userState,
    ) { states ->
        ProfileLibraryState(
            loading = states[0] as LoadingState,
            items = states[1] as ImmutableList<LibraryItem>?,
            filter = states[2] as LibraryFilter?,
            error = states[3] as Exception?,
            user = states[4] as User?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
