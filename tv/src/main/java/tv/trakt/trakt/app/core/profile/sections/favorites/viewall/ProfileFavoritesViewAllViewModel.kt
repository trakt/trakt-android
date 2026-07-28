package tv.trakt.trakt.app.core.profile.sections.favorites.viewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.profile.ProfileConfig.FAVORITES_ALL_PAGE_LIMIT
import tv.trakt.trakt.app.core.profile.sections.favorites.model.interleaveFavorites
import tv.trakt.trakt.app.core.profile.sections.favorites.usecases.GetFavoriteMoviesUseCase
import tv.trakt.trakt.app.core.profile.sections.favorites.usecases.GetFavoriteShowsUseCase
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation

internal class ProfileFavoritesViewAllViewModel(
    private val getFavoriteShowsCase: GetFavoriteShowsUseCase,
    private val getFavoriteMoviesCase: GetFavoriteMoviesUseCase,
) : ViewModel() {
    private val initialState = ProfileFavoritesViewAllState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        if (loadingState.value) {
            return
        }
        viewModelScope.launch {
            try {
                itemsState.update { null }
                loadingState.update { true }

                itemsState.update {
                    coroutineScope {
                        val shows = async {
                            getFavoriteShowsCase.getFavoriteShows(limit = FAVORITES_ALL_PAGE_LIMIT)
                        }
                        val movies = async {
                            getFavoriteMoviesCase.getFavoriteMovies(limit = FAVORITES_ALL_PAGE_LIMIT)
                        }
                        interleaveFavorites(shows.await(), movies.await()).toImmutableList()
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        itemsState,
        errorState,
    ) { loading, items, error ->
        ProfileFavoritesViewAllState(
            isLoading = loading,
            items = items,
            error = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
