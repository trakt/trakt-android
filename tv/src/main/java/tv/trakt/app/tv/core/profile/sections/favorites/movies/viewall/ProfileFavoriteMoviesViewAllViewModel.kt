package tv.trakt.app.tv.core.profile.sections.favorites.movies.viewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.app.tv.core.profile.ProfileConfig.FAVORITES_ALL_PAGE_LIMIT
import tv.trakt.app.tv.core.profile.sections.favorites.movies.usecases.GetFavoriteMoviesUseCase
import tv.trakt.app.tv.helpers.extensions.nowUtc
import tv.trakt.app.tv.helpers.extensions.rethrowCancellation
import java.time.ZonedDateTime

internal class ProfileFavoriteMoviesViewAllViewModel(
    private val getFavoriteMoviesCase: GetFavoriteMoviesUseCase,
) : ViewModel() {
    private val initialState = ProfileFavoriteMoviesViewAllState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)

    private var nextDataPage: Int = 1
    private var hasMoreData: Boolean = true

    private var loadedAt: ZonedDateTime? = null

    init {
        loadData()
    }

    private fun loadData() {
        if (loadingState.value || loadingPageState.value) {
            return
        }
        viewModelScope.launch {
            try {
                nextDataPage = 1
                itemsState.update { null }
                loadingState.update { true }

                itemsState.update {
                    getFavoriteMoviesCase.getFavoriteMovies(limit = FAVORITES_ALL_PAGE_LIMIT)
                }

                nextDataPage += 1
                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
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

                val items = getFavoriteMoviesCase.getFavoriteMovies(
                    limit = FAVORITES_ALL_PAGE_LIMIT,
                    page = nextDataPage,
                )

                itemsState.update {
                    it?.toPersistentList()?.plus(items)
                }

                hasMoreData = (items.size >= FAVORITES_ALL_PAGE_LIMIT)
                nextDataPage += 1
                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingPageState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        loadingPageState,
        itemsState,
        errorState,
    ) { s1, s2, s3, s4 ->
        ProfileFavoriteMoviesViewAllState(
            isLoading = s1,
            isLoadingPage = s2,
            items = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
