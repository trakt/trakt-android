package tv.trakt.trakt.core.profile.sections.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.leaderboard.LeaderboardEntry
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.profile.sections.leaderboard.usecases.GetLeaderboardUseCase
import tv.trakt.trakt.core.profile.sections.leaderboard.usecases.weaveLeaderboardViewer

private const val LEADERBOARD_LIMIT = 100

@Suppress("UNCHECKED_CAST")
internal class LeaderboardViewModel(
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = LeaderboardState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private var entries: List<LeaderboardEntry> = emptyList()
    private var viewer: LeaderboardEntry? = null
    private var pages = 1
    private var hasMoreData = false

    init {
        loadData()

        analytics.logScreenView(
            screenName = "profile_leaderboard",
        )
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }
                errorState.update { null }

                coroutineScope {
                    val viewerAsync = async { loadViewer() }
                    val entriesAsync = async {
                        getLeaderboardUseCase.getLeaderboard(
                            Pagination(page = 1, limit = LEADERBOARD_LIMIT),
                        )
                    }

                    entries = entriesAsync.await()
                    viewer = viewerAsync.await()
                }

                pages = 1
                hasMoreData = entries.size >= LEADERBOARD_LIMIT
                itemsState.update { weaveLeaderboardViewer(entries, viewer) }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private suspend fun loadViewer(): LeaderboardEntry? {
        return try {
            getLeaderboardUseCase.getViewerEntry()
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            null
        }
    }

    fun loadMoreData() {
        if (entries.isEmpty() || !hasMoreData) {
            return
        }
        if (loadingMoreState.value.isLoading || loadingState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingMoreState.update { Loading }

                val nextEntries = getLeaderboardUseCase.getLeaderboard(
                    Pagination(page = pages + 1, limit = LEADERBOARD_LIMIT),
                )

                entries = (entries + nextEntries).distinctBy { it.user.ids.trakt }
                pages += 1
                hasMoreData = nextEntries.size >= LEADERBOARD_LIMIT

                itemsState.update { weaveLeaderboardViewer(entries, viewer) }
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

    val state = combine(
        itemsState,
        loadingState,
        loadingMoreState,
        errorState,
    ) { state ->
        LeaderboardState(
            items = state[0] as ImmutableList<LeaderboardEntry>?,
            loading = state[1] as LoadingState,
            loadingMore = state[2] as LoadingState,
            error = state[3] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
