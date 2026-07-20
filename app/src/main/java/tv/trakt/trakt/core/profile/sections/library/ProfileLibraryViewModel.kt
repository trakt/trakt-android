@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.profile.sections.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.library.LibraryItem
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserLibraryUseCase
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.library.model.LibraryFilter
import tv.trakt.trakt.core.lists.ListsConfig.LIBRARY_PAGE_LIMIT
import tv.trakt.trakt.core.lists.ListsConfig.LIBRARY_SECTION_LIMIT
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

@OptIn(FlowPreview::class)
internal class ProfileLibraryViewModel(
    private val loadLibraryUseCase: LoadUserLibraryUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val sessionManager: SessionManager,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = ProfileLibraryState()

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)

    private val filterState = MutableStateFlow(initialState.filter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val collapseState = MutableStateFlow(isCollapsed())
    private val errorState = MutableStateFlow(initialState.error)

    private var loadDataJob: Job? = null
    private var collapseJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val filter = filterState.value
                val isLoaded = loadLibraryUseCase.isLoaded()

                val localItems = when {
                    isLoaded -> loadLibraryUseCase.loadLocalMedia(LIBRARY_PAGE_LIMIT).items
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
                    loadingState.update { Done }
                } else {
                    loadingState.update { Loading }
                }

                itemsState.update {
                    loadLibraryUseCase.loadMedia(
                        page = 1,
                        limit = LIBRARY_PAGE_LIMIT,
                        availableOn = null,
                    )
                        .items
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
                loadingState.update { Done }
                loadDataJob = null
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                emptyList<LibraryItem>().toImmutableList()
            }
            loadingState.update { Done }
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

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.PROFILE_LIBRARY)
                else -> collapsingManager.expand(CollapsingKey.PROFILE_LIBRARY)
            }
        }
    }

    private fun isCollapsed(): Boolean {
        return collapsingManager.isCollapsed(CollapsingKey.PROFILE_LIBRARY)
    }

    val state = combine(
        loadingState,
        itemsState,
        filterState,
        collapseState,
        errorState,
        userState,
    ) { states ->
        ProfileLibraryState(
            loading = states[0] as LoadingState,
            items = states[1] as ImmutableList<LibraryItem>?,
            filter = states[2] as LibraryFilter?,
            collapsed = states[3] as Boolean,
            error = states[4] as Exception?,
            user = states[5] as User?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
