package tv.trakt.trakt.core.summary.sentiment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.summary.sentiment.navigation.SentimentDestination
import tv.trakt.trakt.core.summary.sentiment.usecases.GetMockSentimentUseCase

internal class SentimentViewModel(
    savedStateHandle: SavedStateHandle,
    private val getMockSentimentUseCase: GetMockSentimentUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<SentimentDestination>()

    private val initialState = SentimentState(
        sentiments = Json.decodeFromString<Sentiments>(destination.sentimentsDto),
        backgroundUrl = destination.mediaImage,
        mediaTitle = destination.mediaTitle,
    )

    private val userState = MutableStateFlow(initialState.user)
    private val sentimentState = MutableStateFlow(initialState.sentiments)

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                val user = sessionManager.getProfile()
                userState.update { user }

                if (user?.isAnyVip != true) {
                    sentimentState.update {
                        getMockSentimentUseCase.getMockSentiments()
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        userState,
        sentimentState,
    ) { state ->
        SentimentState(
            user = state[0] as? User,
            sentiments = state[1] as Sentiments,
            backgroundUrl = initialState.backgroundUrl,
            mediaTitle = initialState.mediaTitle,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
