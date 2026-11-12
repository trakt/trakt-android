package tv.trakt.trakt.core.summary.sentiment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
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
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.realitycheck.RealityCheck
import tv.trakt.trakt.core.summary.sentiment.usecases.GetRealityCheckUseCase
import tv.trakt.trakt.core.summary.sentiment.usecases.GetSentimentUseCase
import tv.trakt.trakt.helpers.collapsing.CollapseDelegate
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import kotlin.time.Duration.Companion.milliseconds

@Suppress("UNCHECKED_CAST")
internal class MediaSentimentViewModel(
    private val mediaId: TraktId,
    private val mediaType: MediaType,
    private val getSentimentUseCase: GetSentimentUseCase,
    private val getRealityCheckUseCase: GetRealityCheckUseCase,
    private val sessionManager: SessionManager,
    collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = MediaSentimentState()

    private val userState = MutableStateFlow(initialState.user)
    private val sentimentState = MutableStateFlow(initialState.sentiment)
    private val realityCheckState = MutableStateFlow(initialState.realityCheck)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val collapse = CollapseDelegate(
        manager = collapsingManager,
        scope = viewModelScope,
        key = when (mediaType) {
            MediaType.Movie -> CollapsingKey.MOVIE_SENTIMENT
            MediaType.Show -> CollapsingKey.SHOW_SENTIMENT
            MediaType.Season, MediaType.Episode -> error("Sentiment is not available for $mediaType")
        },
    )

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
                val user = userState.value
                user != null && user.isAnyVip != it?.isAnyVip
            }
            .onEach { user ->
                userState.update { user }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                loadUser()

                val sentiment = async { loadSentiment() }
                val realityCheck = async { loadRealityCheck() }

                sentimentState.update { sentiment.await() }
                realityCheckState.update { realityCheck.await() }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private suspend fun loadSentiment(): Sentiments? {
        return try {
            getSentimentUseCase.getSentiments(
                mediaId = mediaId,
                mediaType = mediaType,
            )
        } catch (error: Exception) {
            error.rethrowCancellation {
                errorState.update { error }
                Timber.recordError(error)
            }
            null
        }
    }

    private suspend fun loadRealityCheck(): RealityCheck? {
        return try {
            getRealityCheckUseCase.getRealityCheck(
                mediaId = mediaId,
                mediaType = mediaType,
            )
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            null
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

    fun setCollapsed(collapsed: Boolean) {
        collapse.set(collapsed)
    }

    val state = combine(
        userState,
        sentimentState,
        loadingState,
        collapse.state,
        errorState,
        realityCheckState,
    ) { state ->
        MediaSentimentState(
            user = state[0] as User?,
            sentiment = state[1] as Sentiments?,
            loading = state[2] as LoadingState,
            collapsed = state[3] as Boolean,
            error = state[4] as Exception?,
            realityCheck = state[5] as RealityCheck?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
