package tv.trakt.app.tv.core.details.lists.details.movies

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.details.lists.details.CustomListDetailsConfig.CUSTOM_LIST_PAGE_LIMIT
import tv.trakt.app.tv.core.details.lists.details.movies.navigation.CustomListMoviesDestination
import tv.trakt.app.tv.core.details.lists.details.movies.usecases.GetListItemsUseCase
import tv.trakt.app.tv.helpers.extensions.rethrowCancellation

internal class CustomListMoviesViewModel(
    savedStateHandle: SavedStateHandle,
    private val getListItemsUseCase: GetListItemsUseCase,
) : ViewModel() {
    private val initialState = CustomListMoviesState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val moviesState = MutableStateFlow(initialState.movies)
    private val errorState = MutableStateFlow(initialState.error)

    val destination = savedStateHandle.toRoute<CustomListMoviesDestination>()

    private var nextDataPage: Int = 1
    private var hasMoreData: Boolean = true

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }
                val movies = getListItemsUseCase.getListItems(
                    listId = TraktId(destination.listId),
                    page = nextDataPage,
                )
                moviesState.update { movies }
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Log.e("CustomListMoviesViewModel", "Error loading movies for list: ${destination.listId} $error")
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun loadNextDataPage() {
        if (loadingPageState.value || !hasMoreData) {
            return
        }
        viewModelScope.launch {
            try {
                loadingPageState.update { true }

                val movies = getListItemsUseCase.getListItems(
                    listId = TraktId(destination.listId),
                    page = nextDataPage,
                )

                moviesState.update {
                    it?.toPersistentList()?.plus(movies)
                }

                hasMoreData = (movies.size >= CUSTOM_LIST_PAGE_LIMIT)
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Log.e(
                        "CustomListMoviesViewModel",
                        "Error loading next page of movies for list: ${destination.listId} $error",
                    )
                }
            } finally {
                loadingPageState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        loadingPageState,
        moviesState,
        errorState,
    ) { s1, s2, s3, s4 ->
        CustomListMoviesState(
            isLoading = s1,
            isLoadingPage = s2,
            movies = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
