package tv.trakt.trakt.app.core.movies.features.releases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.movies.usecase.GetMoviesReleasesUseCase
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation

internal class MoviesReleasesViewAllViewModel(
    private val getItemsUseCase: GetMoviesReleasesUseCase,
) : ViewModel() {
    private val initialState = MoviesReleasesViewAllState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }

                val items = getItemsUseCase.getReleases(
                    limit = 100,
                    range = 60,
                )
                itemsState.update { items }
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
    ) { s1, s2, s3 ->
        MoviesReleasesViewAllState(
            isLoading = s1,
            items = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
