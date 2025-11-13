package tv.trakt.trakt.core.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.discover.DiscoverState.UserState
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase

@OptIn(FlowPreview::class)
internal class DiscoverViewModel(
    private val modeManager: MediaModeManager,
    private val sessionManager: SessionManager,
    private val userWatchlistUseCase: LoadUserWatchlistUseCase,
    private val userProgressUseCase: LoadUserProgressUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val userProgressLocalSource: UserProgressLocalDataSource,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = DiscoverState()

    private val modeState = MutableStateFlow(modeManager.getMode())
    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val collectionState = MutableStateFlow(initialState.collection)
    private val userState = MutableStateFlow(initialState.user)

    init {
        loadBackground()

        observeUser()
        observeMode()
        observeData()

        analytics.logScreenView(
            screenName = "discover",
        )
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    private fun observeMode() {
        modeManager.observeMode()
            .onEach { value ->
                modeState.update { value }
            }
            .launchIn(viewModelScope)
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .onEach { user ->
                userState.update {
                    UserState(
                        user = user,
                        loading = LoadingState.DONE,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeData() {
        merge(
            userProgressLocalSource.observeUpdates(),
            userWatchlistLocalSource.observeUpdates(),
        )
            .distinctUntilChanged()
            .onStart {
                loadProgress()
                loadWatchlist()
            }
            .onEach {
                loadProgress()
                loadWatchlist()
            }
            .launchIn(viewModelScope)
    }

    private fun loadWatchlist() {
        viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    collectionState.update {
                        DiscoverCollectionState.Default
                    }
                    return@launch
                }

                coroutineScope {
                    if (!userWatchlistUseCase.isShowsLoaded() && !userWatchlistUseCase.isMoviesLoaded()) {
                        userWatchlistUseCase.loadWatchlist()
                    }

                    val watchlistShowsAsync = async { userWatchlistUseCase.loadLocalShows() }
                    val watchlistMoviesAsync = async { userWatchlistUseCase.loadLocalMovies() }

                    val watchlistShows = when {
                        modeState.value.isMediaOrShows -> watchlistShowsAsync.await()
                        else -> emptySet()
                    }
                    val watchlistMovies = when {
                        modeState.value.isMediaOrMovies -> watchlistMoviesAsync.await()
                        else -> emptySet()
                    }

                    collectionState.update { state ->
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

    private fun loadProgress() {
        viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    collectionState.update {
                        DiscoverCollectionState.Default
                    }
                    return@launch
                }

                if (!userProgressUseCase.isShowsLoaded() && !userProgressUseCase.isMoviesLoaded()) {
                    userProgressUseCase.loadProgress()
                }

                val progressShowsAsync = async { userProgressUseCase.loadLocalShows() }
                val progressMoviesAsync = async { userProgressUseCase.loadLocalMovies() }

                coroutineScope {
                    val progressShows = when {
                        modeState.value.isMediaOrShows -> progressShowsAsync.await()
                        else -> emptySet()
                    }
                    val progressMovies = when {
                        modeState.value.isMediaOrMovies -> progressMoviesAsync.await()
                        else -> emptySet()
                    }

                    collectionState.update { state ->
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
    }

    val state = combine(
        backgroundState,
        collectionState,
        userState,
    ) { s1, s2, s3 ->
        DiscoverState(
            backgroundUrl = s1,
            collection = s2,
            user = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
