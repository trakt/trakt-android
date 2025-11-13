package tv.trakt.trakt.core.user

import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase

internal class CollectionStateProvider(
    private val sessionManager: SessionManager,
    private val userWatchlistUseCase: LoadUserWatchlistUseCase,
    private val userProgressUseCase: LoadUserProgressUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val userProgressLocalSource: UserProgressLocalDataSource,
) {
    private val _stateFlow = MutableStateFlow(UserCollectionState.Default)
    val stateFlow = _stateFlow.asStateFlow()

    private val loadingLock = Mutex()

    @OptIn(FlowPreview::class)
    fun launchIn(scope: CoroutineScope) {
        merge(
            userProgressLocalSource.observeUpdates(),
            userWatchlistLocalSource.observeUpdates(),
        )
            .distinctUntilChanged()
            .onStart { loadData() }
            .onEach { loadData() }
            .launchIn(scope)
    }

    private suspend fun loadData() {
        if (!loadingLock.tryLock()) {
            Timber.d("User collection state is already loading, skipping")
            return
        }

        try {
            Timber.d("Loading user collection state")
            coroutineScope {
                val progress = async { loadProgress() }
                val watchlist = async { loadWatchlist() }
                awaitAll(progress, watchlist)
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.e(error, "Failed to load user collection state")
            }
        } finally {
            loadingLock.unlock()
        }
    }

    private suspend fun loadProgress() {
        try {
            if (!sessionManager.isAuthenticated()) {
                _stateFlow.update {
                    UserCollectionState.Default
                }
                return
            }

            if (!userProgressUseCase.isLoaded()) {
                userProgressUseCase.loadProgress()
            }

            coroutineScope {
                val progressShowsAsync = async { userProgressUseCase.loadLocalShows() }
                val progressMoviesAsync = async { userProgressUseCase.loadLocalMovies() }

                val progressShows = progressShowsAsync.await()
                val progressMovies = progressMoviesAsync.await()

                _stateFlow.update { state ->
                    state.copy(
                        watchedShows = progressShows
                            .filter { it.isCompleted }
                            .asyncMap { it.mediaId }
                            .toImmutableSet(),
                        watchedMovies = progressMovies
                            .asyncMap { it.mediaId }
                            .toImmutableSet(),
                    )
                }
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.e(error, "Failed to load watched data")
            }
        }
    }

    private suspend fun loadWatchlist() {
        try {
            if (!sessionManager.isAuthenticated()) {
                _stateFlow.update {
                    UserCollectionState.Default
                }
                return
            }

            coroutineScope {
                if (!userWatchlistUseCase.isLoaded()) {
                    userWatchlistUseCase.loadWatchlist()
                }

                val watchlistShowsAsync = async { userWatchlistUseCase.loadLocalShows() }
                val watchlistMoviesAsync = async { userWatchlistUseCase.loadLocalMovies() }

                val watchlistShows = watchlistShowsAsync.await()
                val watchlistMovies = watchlistMoviesAsync.await()

                _stateFlow.update { state ->
                    state.copy(
                        watchlistShows = watchlistShows.asyncMap { it.id }.toImmutableSet(),
                        watchlistMovies = watchlistMovies.asyncMap { it.id }.toImmutableSet(),
                    )
                }
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.e(error, "Failed to load watchlist data")
            }
        }
    }
}
