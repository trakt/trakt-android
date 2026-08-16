package tv.trakt.trakt.core.klipy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.klipy.data.remote.GifsRemoteDataSource
import tv.trakt.trakt.common.core.klipy.model.GIFS_DEFAULT_PER_PAGE
import tv.trakt.trakt.common.core.klipy.model.Gif
import tv.trakt.trakt.common.core.klipy.model.GifPage
import tv.trakt.trakt.common.core.klipy.model.GifsQuery
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val SEARCH_DEBOUNCE = 350.milliseconds

@Suppress("UNCHECKED_CAST")
internal class GifPickerViewModel(
    private val remoteSource: GifsRemoteDataSource,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = GifPickerState()

    private val queryState = MutableStateFlow(initialState.query)
    private val gifsState = MutableStateFlow(initialState.gifs)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var page = 1
    private var hasNextPage = false
    private var trending: TrendingGifs? = null
    private var loadJob: Job? = null
    private var loadMoreJob: Job? = null
    private var userId: TraktId? = null

    init {
        viewModelScope.launch {
            userId = sessionManager.getProfile()?.ids?.trakt
            loadGifs(debounce = Duration.ZERO)
        }
    }

    fun updateQuery(query: String) {
        if (queryState.value == query) {
            return
        }

        queryState.update { query }
        loadGifs(debounce = SEARCH_DEBOUNCE)
    }

    fun loadMore() {
        if (!hasNextPage) return
        if (loadingState.value.isLoading || loadingMoreState.value.isLoading) return

        loadMoreJob = viewModelScope.launch {
            try {
                loadingMoreState.update { LoadingState.Loading }

                val result = loadPage(page = page + 1)
                page = result.page
                hasNextPage = result.hasNext
                gifsState.update { current -> (current + result.items).toImmutableList() }
                cacheTrending()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                if (isActive) {
                    loadingMoreState.update { LoadingState.Done }
                }
            }
        }
    }

    /**
     * Loads the first page for the current query. An empty query means trending, which is kept
     * in memory so clearing the input restores it without another round trip.
     */
    private fun loadGifs(debounce: Duration) {
        loadJob?.cancel()
        loadMoreJob?.cancel()

        if (queryState.value.isBlank()) {
            trending?.let { cached ->
                page = cached.page
                hasNextPage = cached.hasNext
                gifsState.update { cached.gifs }
                loadingState.update { LoadingState.Done }
                errorState.update { null }
                return
            }
        }

        loadJob = viewModelScope.launch {
            try {
                loadingState.update { LoadingState.Loading }
                errorState.update { null }
                gifsState.update { persistentListOf() }

                delay(debounce) // Debounce user input.

                val result = loadPage(page = 1)

                page = result.page
                hasNextPage = result.hasNext
                gifsState.update { result.items }

                cacheTrending()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                if (isActive) {
                    loadingState.update { LoadingState.Done }
                }
            }
        }
    }

    private suspend fun loadPage(page: Int): GifPage {
        val term = queryState.value.trim()
        val query = GifsQuery(
            term = term.ifEmpty { null },
            pagination = Pagination(page = page, limit = GIFS_DEFAULT_PER_PAGE),
            customerId = userId.toString(),
        )

        return when {
            term.isEmpty() -> remoteSource.getTrendingGifs(query)
            else -> remoteSource.searchGifs(query)
        }
    }

    private fun cacheTrending() {
        if (queryState.value.isNotBlank()) return

        trending = TrendingGifs(
            gifs = gifsState.value,
            page = page,
            hasNext = hasNextPage,
        )
    }

    private data class TrendingGifs(
        val gifs: ImmutableList<Gif>,
        val page: Int,
        val hasNext: Boolean,
    )

    val state = combine(
        queryState,
        gifsState,
        loadingState,
        loadingMoreState,
        errorState,
    ) { state ->
        GifPickerState(
            query = state[0] as String,
            gifs = state[1] as ImmutableList<Gif>,
            loading = state[2] as LoadingState,
            loadingMore = state[3] as LoadingState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
