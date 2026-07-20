@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.userprofile.sections.favorites.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.userprofile.sections.favorites.usecases.GetUserProfileFavoritesUseCase

internal class AllUserProfileFavoritesViewModel(
    savedStateHandle: SavedStateHandle,
    private val getFavoritesUseCase: GetUserProfileFavoritesUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<AllUserProfileFavoritesDestination>()
    private val userId = destination.userId.toTraktId()

    private val initialState = AllUserProfileFavoritesState()

    private val itemsState = MutableStateFlow<ImmutableList<FavoriteItem>?>(null)
    private val filterState = MutableStateFlow(initialState.filter)
    private val sortingState = MutableStateFlow(initialState.sorting)
    private val navigateShowState = MutableStateFlow(initialState.navigateShow)
    private val navigateMovieState = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadInitialData()
        observeData()
    }

    private fun observeData() {
        collectionStateProvider.launchIn(viewModelScope)
    }

    private fun loadInitialData() {
        dataJob = viewModelScope.launch {
            try {
                loadingState.update { Loading }
                itemsState.update {
                    getFavoritesUseCase.getUserFavorites(
                        userId = userId,
                        mode = filterState.value,
                        sorting = sortingState.value,
                    )
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
                itemsState.update {
                    getFavoritesUseCase.getUserFavorites(
                        userId = userId,
                        mode = filterState.value,
                        sorting = sortingState.value,
                    )
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

    fun setFilter(mode: MediaMode) {
        if (mode == filterState.value) {
            return
        }
        filterState.update { mode }
        loadData()
    }

    fun setSorting(newSorting: Sorting) {
        if (newSorting == sortingState.value) {
            return
        }
        sortingState.update { newSorting }
        loadData()
    }

    fun navigateToShow(show: Show) {
        if (navigateShowState.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            navigateShowState.update { show.ids.trakt }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovieState.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                movieLocalDataSource.upsertMovies(listOf(movie))
                navigateMovieState.update { movie.ids.trakt }
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
        navigateShowState.update { null }
        navigateMovieState.update { null }
    }

    val state = combine(
        itemsState,
        filterState,
        sortingState,
        collectionStateProvider.stateFlow,
        navigateShowState,
        navigateMovieState,
        loadingState,
        errorState,
    ) { state ->
        AllUserProfileFavoritesState(
            items = state[0] as ImmutableList<FavoriteItem>?,
            filter = state[1] as MediaMode,
            sorting = state[2] as Sorting,
            collection = state[3] as UserCollectionState,
            navigateShow = state[4] as TraktId?,
            navigateMovie = state[5] as TraktId?,
            loading = state[6] as LoadingState,
            error = state[7] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
