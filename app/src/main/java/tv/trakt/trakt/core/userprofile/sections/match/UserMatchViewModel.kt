package tv.trakt.trakt.core.userprofile.sections.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.UserMatch
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.userprofile.sections.match.usecases.GetUserMatchUseCase
import kotlin.time.Duration.Companion.milliseconds

private const val SHARED_MEDIA_LIMIT = 6

@OptIn(FlowPreview::class)
@Suppress("UNCHECKED_CAST")
internal class UserMatchViewModel(
    private val user: User,
    private val getUserMatchUseCase: GetUserMatchUseCase,
    private val sessionManager: SessionManager,
    private val filterManager: GlobalFilterManager,
    private val dataChanges: DataChanges,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = UserMatchState()

    private val viewerState = MutableStateFlow(initialState.viewer)
    private val matchState = MutableStateFlow(initialState.match)
    private val sharedMoviesState = MutableStateFlow(initialState.sharedMovies)
    private val sharedShowsState = MutableStateFlow(initialState.sharedShows)
    private val loadingState = MutableStateFlow(initialState.loading)

    private var mode = filterManager.getFilter().mode

    private var dataJob: Job? = null
    private var sharedMediaJob: Job? = null

    init {
        loadData()
        observeMode()
        observeFavorites()
    }

    private fun observeMode() {
        filterManager.observeFilter()
            .map { it.mode }
            .distinctUntilChanged()
            .filter { it != mode }
            .onEach {
                mode = it
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun observeFavorites() {
        dataChanges.observe(Kind.Favorites)
            .debounce(200.milliseconds)
            .onEach { loadData() }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated()) return@launch

                val viewer = sessionManager.getProfile() ?: return@launch
                if (viewer.ids.trakt == user.ids.trakt) return@launch

                viewerState.update { viewer }
                if (matchState.value == null) {
                    loadingState.update { Loading }
                }

                val match = getUserMatchUseCase.getMatch(
                    userSlug = user.ids.slug,
                    mode = mode,
                )
                val sharedMediaRequested = sharedMoviesState.value != null || sharedMediaJob?.isActive == true
                sharedMediaJob?.cancel()
                sharedMoviesState.update { null }
                sharedShowsState.update { null }

                matchState.update { match }
                if (sharedMediaRequested) {
                    loadSharedMedia()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun openDetails() {
        analytics.logDrilldown(source = "match", type = null)
        loadSharedMedia()
    }

    private fun loadSharedMedia() {
        val match = matchState.value ?: return
        if (sharedMoviesState.value != null || sharedMediaJob?.isActive == true) {
            return
        }

        sharedMediaJob = viewModelScope.launch {
            launch {
                val movies = getUserMatchUseCase.getMovies(match.sharedMovies.take(SHARED_MEDIA_LIMIT))
                sharedMoviesState.update { movies }
            }
            launch {
                val shows = getUserMatchUseCase.getShows(match.sharedShows.take(SHARED_MEDIA_LIMIT))
                sharedShowsState.update { shows }
            }
        }
    }

    val state = combine(
        viewerState,
        matchState,
        sharedMoviesState,
        sharedShowsState,
        loadingState,
    ) { state ->
        UserMatchState(
            viewer = state[0] as User?,
            match = state[1] as UserMatch?,
            sharedMovies = state[2] as ImmutableList<Movie>?,
            sharedShows = state[3] as ImmutableList<Show>?,
            loading = state[4] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
