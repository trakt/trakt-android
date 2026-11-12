package tv.trakt.trakt.core.summary.soundtrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_NOT_FOUND
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import tv.trakt.trakt.core.billing.model.VipUpsellSource
import tv.trakt.trakt.core.summary.soundtrack.usecases.GetSoundtrackUseCase
import tv.trakt.trakt.helpers.collapsing.CollapseDelegate
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import kotlin.time.Duration.Companion.milliseconds

private const val UNKNOWN_MATCH = "unknown"

@Suppress("UNCHECKED_CAST")
internal class SoundtrackViewModel(
    private val mediaType: MediaType,
    private val mediaId: TraktId,
    private val getSoundtrackUseCase: GetSoundtrackUseCase,
    private val collapsingManager: CollapsingManager,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val collapsingKey = when (mediaType) {
        MediaType.Show -> CollapsingKey.SHOW_SOUNDTRACK
        MediaType.Movie, MediaType.Season, MediaType.Episode -> CollapsingKey.MOVIE_SOUNDTRACK
    }
    private val initialState = SoundtrackState()

    private val userState = MutableStateFlow(initialState.user)
    private val tracksState = MutableStateFlow(initialState.tracks)
    private val collapse = CollapseDelegate(
        manager = collapsingManager,
        scope = viewModelScope,
        key = collapsingKey,
    )
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadJob: Job? = null

    init {
        loadData()
        observeUser()
    }

    @OptIn(FlowPreview::class)
    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .filter { userState.value?.isAnyVip != it?.isAnyVip }
            .onEach { loadData() }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val user = loadUser()
                userState.update { user }

                // The endpoint is VIP gated and returns an empty list to everyone else.
                if (user?.isAnyVip != true) {
                    tracksState.update { persistentListOf() }
                    return@launch
                }

                tracksState.update {
                    getSoundtrackUseCase.getSoundtrack(
                        type = mediaType,
                        mediaId = mediaId,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    tracksState.update { persistentListOf() }
                    if (error.getHttpCode() != HTTP_ERROR_NOT_FOUND) {
                        errorState.update { error }
                        Timber.recordError(error)
                    }
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private suspend fun loadUser(): User? {
        return try {
            sessionManager.getProfile()
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            null
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapse.set(collapsed)
    }

    fun logPlay(
        track: SoundtrackTrack,
        source: String,
    ) {
        analytics.playback.logSoundtrack(
            source = source,
            position = track.position,
            matchedOn = track.matchedOn ?: UNKNOWN_MATCH,
        )
    }

    fun logUpsellSeen() {
        analytics.vip.logUpsellImpression(VipUpsellSource.Soundtrack.value)
    }

    val state = combine(
        userState,
        tracksState,
        collapse.state,
        loadingState,
        errorState,
    ) { state ->
        SoundtrackState(
            user = state[0] as User?,
            tracks = state[1] as ImmutableList<SoundtrackTrack>?,
            collapsed = state[2] as Boolean,
            loading = state[3] as LoadingState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
