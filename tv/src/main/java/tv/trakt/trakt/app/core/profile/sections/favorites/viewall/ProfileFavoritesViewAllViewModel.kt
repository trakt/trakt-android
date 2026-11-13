package tv.trakt.trakt.app.core.profile.sections.favorites.viewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
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
import tv.trakt.trakt.app.core.profile.sections.favorites.model.FavoriteItem
import tv.trakt.trakt.app.core.profile.sections.favorites.model.getFavoritesSorting
import tv.trakt.trakt.app.core.profile.sections.favorites.model.interleaveFavorites
import tv.trakt.trakt.app.core.profile.sections.favorites.usecases.GetFavoriteMoviesUseCase
import tv.trakt.trakt.app.core.profile.sections.favorites.usecases.GetFavoriteShowsUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.analytics.logListSort
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.sorting.Sorting

@Suppress("UNCHECKED_CAST")
internal class ProfileFavoritesViewAllViewModel(
    private val getFavoriteShowsCase: GetFavoriteShowsUseCase,
    private val getFavoriteMoviesCase: GetFavoriteMoviesUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ProfileFavoritesViewAllState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)
    private val sortingState = MutableStateFlow(initialState.sorting)

    init {
        loadData()
    }

    private fun loadData() {
        if (loadingState.value) {
            return
        }
        viewModelScope.launch {
            try {
                loadingState.update { true }

                val sorting = sortingState.value
                itemsState.update {
                    coroutineScope {
                        val shows = async {
                            getFavoriteShowsCase.getFavoriteShows(
                                sorting = sorting,
                                limit = FAVORITES_ALL_PAGE_LIMIT,
                            )
                        }
                        val movies = async {
                            getFavoriteMoviesCase.getFavoriteMovies(
                                sorting = sorting,
                                limit = FAVORITES_ALL_PAGE_LIMIT,
                            )
                        }
                        interleaveFavorites(shows.await(), movies.await())
                            .sortedWith(getFavoritesSorting(sorting))
                            .toImmutableList()
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

    fun setSorting(sorting: Sorting) {
        if (sorting == sortingState.value || loadingState.value) {
            return
        }

        sortingState.update { sorting }
        loadData()

        analytics.lists.logListSort(sorting)
    }

    val state = combine(
        loadingState,
        itemsState,
        errorState,
        sortingState,
    ) { state ->
        ProfileFavoritesViewAllState(
            isLoading = state[0] as Boolean,
            items = state[1] as ImmutableList<FavoriteItem>?,
            error = state[2] as Exception?,
            sorting = state[3] as Sorting,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
