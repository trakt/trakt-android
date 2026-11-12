package tv.trakt.trakt.core.trivia

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
import tv.trakt.trakt.common.model.trivia.TriviaFact
import tv.trakt.trakt.core.billing.model.VipUpsellSource
import tv.trakt.trakt.core.trivia.usecases.GetTriviaUseCase
import tv.trakt.trakt.helpers.collapsing.CollapseDelegate
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import kotlin.time.Duration.Companion.milliseconds

@Suppress("UNCHECKED_CAST")
internal class MediaTriviaViewModel(
    private val mediaId: TraktId,
    private val mediaType: MediaType,
    private val getTriviaUseCase: GetTriviaUseCase,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
    collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = MediaTriviaState()

    private val userState = MutableStateFlow(initialState.user)
    private val summaryState = MutableStateFlow(initialState.summary)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val collapse = CollapseDelegate(
        manager = collapsingManager,
        scope = viewModelScope,
        key = when (mediaType) {
            MediaType.Movie -> CollapsingKey.MOVIE_TRIVIA
            MediaType.Show -> CollapsingKey.SHOW_TRIVIA
            MediaType.Season, MediaType.Episode -> error("Trivia is not available for $mediaType")
        },
    )

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
            .filter {
                // Only update if VIP status has changed, as trivia may be VIP locked.
                userState.value?.isAnyVip != it?.isAnyVip
            }
            .onEach { user ->
                userState.update { user }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                loadingState.update { Loading }

                loadUser()
                val (summary, items) = getTriviaUseCase.getTrivia(
                    mediaId = mediaId,
                    mediaType = mediaType,
                )

                summaryState.update { summary }
                itemsState.update { items }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    // Ignore 404 errors, as they simply mean no trivia is available.
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

    private suspend fun loadUser() {
        try {
            userState.update {
                sessionManager.getProfile()
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    fun logUpsellSeen() {
        analytics.vip.logUpsellImpression(VipUpsellSource.Trivia.value)
    }

    fun setCollapsed(collapsed: Boolean) {
        collapse.set(collapsed)
    }

    val state = combine(
        userState,
        itemsState,
        summaryState,
        loadingState,
        collapse.state,
        errorState,
    ) { state ->
        MediaTriviaState(
            user = state[0] as User?,
            items = state[1] as ImmutableList<TriviaFact>?,
            summary = state[2] as ImmutableList<String>?,
            loading = state[3] as LoadingState,
            collapsed = state[4] as Boolean,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
