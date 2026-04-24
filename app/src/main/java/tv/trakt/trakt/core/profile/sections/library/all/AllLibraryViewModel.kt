@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.profile.sections.library.all

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
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.library.model.LibraryFilter
import tv.trakt.trakt.core.library.model.LibraryItem
import tv.trakt.trakt.core.lists.ListsConfig.LIBRARY_PAGE_LIMIT
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

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null

    private var page: Int = 1
    private var hasMoreData: Boolean = false

    init {
        loadUser()
        loadData()

        analytics.logScreenView(
            screenName = "all_library",
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

    fun loadData(ignoreErrors: Boolean = false) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                page = 1
                loadingState.update { Loading }

                if (loadEmptyIfNeeded()) {
                    return@launch
                }

                val remoteItems = loadLibraryUseCase.loadMedia(
                    page = page,
                    limit = LIBRARY_PAGE_LIMIT,
                    availableOn = filterState.value?.value,
                )
                itemsState.update {
                    remoteItems.items.toImmutableList()
                }

                hasMoreData = remoteItems.hasMoreData
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
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
                loadingMoreState.update { Loading }

                val nextData = loadLibraryUseCase.loadMedia(
                    page = page + 1,
                    limit = LIBRARY_PAGE_LIMIT,
                    availableOn = filterState.value?.value,
                )

                itemsState.update { items ->
                    items
                        ?.plus(nextData.items)
                        ?.distinctBy { it.key }
                        ?.toImmutableList()
                }

                page += 1
                hasMoreData = nextData.hasMoreData
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingMoreState.update { Done }
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
        if (loadingState.value.isLoading ||
            loadingMoreState.value.isLoading
        ) {
            return
        }
        viewModelScope.launch {
            filterState.update {
                when (it) {
                    newFilter -> null
                    else -> newFilter
                }
            }
            loadData()
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

    val state = combine(
        loadingState,
        loadingMoreState,
        itemsState,
        filterState,
        navigateShow,
        navigateMovie,
        userState,
        errorState,
    ) { state ->
        AllLibraryState(
            loading = state[0] as LoadingState,
            loadingMore = state[1] as LoadingState,
            items = state[2] as? ImmutableList<LibraryItem>,
            filter = state[3] as? LibraryFilter,
            navigateShow = state[4] as? TraktId,
            navigateMovie = state[5] as? TraktId,
            user = state[6] as? User,
            error = state[7] as? Exception,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
