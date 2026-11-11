package tv.trakt.trakt.core.summary.sentiment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.realitycheck.RealityCheck
import tv.trakt.trakt.core.summary.sentiment.model.InsightsTab
import tv.trakt.trakt.core.summary.sentiment.navigation.SentimentDestination
import tv.trakt.trakt.core.summary.sentiment.usecases.GetMockSentimentUseCase

private const val INSIGHTS_OPEN_SOURCE = "card"

@Suppress("UNCHECKED_CAST")
internal class SentimentViewModel(
    savedStateHandle: SavedStateHandle,
    private val getMockSentimentUseCase: GetMockSentimentUseCase,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<SentimentDestination>()

    private val sentiments = destination.sentimentsDto
        ?.let { Json.decodeFromString<Sentiments>(it) }
        ?.takeUnless { it.isEmpty }
    private val realityCheck = destination.realityCheckDto
        ?.let { Json.decodeFromString<RealityCheck>(it) }
    private val tabs = listOfNotNull(
        InsightsTab.Sentiment.takeIf { sentiments != null },
        InsightsTab.RealityCheck.takeIf { realityCheck != null },
    ).toImmutableList()

    private val initialState = SentimentState(
        sentiments = sentiments,
        realityCheck = realityCheck,
        tabs = tabs,
        tab = if (realityCheck != null) InsightsTab.RealityCheck else InsightsTab.Sentiment,
        backgroundUrl = destination.mediaImage,
        mediaTitle = destination.mediaTitle,
    )

    private val userState = MutableStateFlow(initialState.user)
    private val sentimentState = MutableStateFlow(initialState.sentiments)
    private val tabState = MutableStateFlow(initialState.tab)

    init {
        loadUser()

        analytics.logDrilldown(
            source = if (sentiments != null) "sentiment" else "reality-check",
            type = null,
        )
        analytics.logInsightsOpen(
            source = INSIGHTS_OPEN_SOURCE,
            tab = initialState.tab.value,
        )
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                sessionManager.observeProfile()
                    .distinctUntilChangedBy { it?.isAnyVip }
                    .collect { user ->
                        userState.update { user }
                        sentimentState.update {
                            when {
                                sentiments == null -> null
                                user?.isAnyVip == true -> sentiments
                                else -> getMockSentimentUseCase.getMockSentiments()
                            }
                        }
                    }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun selectTab(tab: InsightsTab) {
        if (tabState.value == tab) return

        tabState.update { tab }
        analytics.logInsightsTab(tab.value)
    }

    fun onVipUpsellSeen() {
        analytics.vip.logUpsellImpression(tabState.value.upsellSource.value)
    }

    val state = combine(
        userState,
        sentimentState,
        tabState,
    ) { state ->
        SentimentState(
            user = state[0] as? User,
            sentiments = state[1] as Sentiments?,
            realityCheck = initialState.realityCheck,
            tabs = initialState.tabs,
            tab = state[2] as InsightsTab,
            backgroundUrl = initialState.backgroundUrl,
            mediaTitle = initialState.mediaTitle,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
