package tv.trakt.trakt.core.summary.shows.features.sentiment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
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
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.summary.shows.features.sentiment.usecases.GetShowSentimentUseCase

internal class ShowSentimentViewModel(
    private val show: Show,
    private val getSentimentUseCase: GetShowSentimentUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = ShowSentimentState()

    private val userState = MutableStateFlow(initialState.user)
    private val sentimentState = MutableStateFlow(initialState.sentiment)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        observeUser()
    }

    @OptIn(FlowPreview::class)
    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200)
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
                sentimentState.update {
                    getSentimentUseCase.getSentiments(show.ids.trakt)
                }
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

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        userState,
        sentimentState,
        loadingState,
        errorState,
    ) { state ->
        ShowSentimentState(
            user = state[0] as User?,
            sentiment = state[1] as Sentiments?,
            loading = state[2] as LoadingState,
            error = state[3] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
