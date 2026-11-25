package tv.trakt.trakt.core.summary.sentiment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMatch
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.realitycheck.DocumentaryCheck
import tv.trakt.trakt.common.model.realitycheck.RealityCheck
import tv.trakt.trakt.common.model.recommended.RecommendedSource
import tv.trakt.trakt.core.summary.sentiment.model.InsightsTab
import tv.trakt.trakt.core.summary.sentiment.model.RealityCheckSection
import tv.trakt.trakt.core.summary.sentiment.navigation.SentimentDestination
import tv.trakt.trakt.core.summary.sentiment.usecases.GetMatchReasonsUseCase
import tv.trakt.trakt.core.summary.sentiment.usecases.GetMockSentimentUseCase
import tv.trakt.trakt.helpers.collapsing.CollapseDelegate
import tv.trakt.trakt.helpers.collapsing.CollapsingManager

private const val INSIGHTS_OPEN_SOURCE = "card"

@Suppress("UNCHECKED_CAST")
internal class SentimentViewModel(
    savedStateHandle: SavedStateHandle,
    private val getMockSentimentUseCase: GetMockSentimentUseCase,
    private val getMatchReasonsUseCase: GetMatchReasonsUseCase,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
    collapsingManager: CollapsingManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<SentimentDestination>()

    private val sentiments = destination.sentimentsDto
        ?.let { Json.decodeFromString<Sentiments>(it) }
        ?.takeUnless { it.isEmpty }
    private val realityCheck = destination.realityCheckDto
        ?.let { Json.decodeFromString<RealityCheck>(it) }
    private val documentaryCheck = destination.documentaryCheckDto
        ?.let { Json.decodeFromString<DocumentaryCheck>(it) }
    private val match = destination.matchDto
        ?.let { Json.decodeFromString<MediaMatch>(it) }
        ?.takeIf { it.score != null }
    private val tabs = listOfNotNull(
        InsightsTab.Sentiment.takeIf { sentiments != null },
        InsightsTab.RealityCheck.takeIf { realityCheck != null || documentaryCheck != null },
        InsightsTab.Match.takeIf { match != null },
    ).toImmutableList()

    private val initialState = SentimentState(
        sentiments = sentiments,
        realityCheck = realityCheck,
        documentaryCheck = documentaryCheck,
        match = match,
        tabs = tabs,
        tab = destination.tab.takeIf { it in tabs } ?: tabs.firstOrNull() ?: InsightsTab.Sentiment,
        backgroundUrl = destination.mediaImage,
        mediaTitle = destination.mediaTitle,
    )

    private val userState = MutableStateFlow(initialState.user)
    private val sentimentState = MutableStateFlow(initialState.sentiments)
    private val tabState = MutableStateFlow(initialState.tab)
    private val matchState = MutableStateFlow(initialState.match)
    private val matchReasonsState = MutableStateFlow(initialState.matchReasons)
    private val matchReasonsLoadingState = MutableStateFlow(initialState.matchReasonsLoading)
    private val sectionCollapses = RealityCheckSection.entries.associateWith { section ->
        CollapseDelegate(
            manager = collapsingManager,
            scope = viewModelScope,
            key = section.collapsingKey,
        )
    }
    private var matchOwnerId: TraktId? = null
    private val collapsedSectionsState = combine(
        sectionCollapses.map { (section, collapse) ->
            collapse.state.map { collapsed -> section.takeIf { collapsed } }
        },
    ) { sections ->
        sections.filterNotNull().toImmutableSet()
    }

    init {
        loadUser()

        analytics.logDrilldown(
            source = when {
                sentiments != null -> "sentiment"
                realityCheck != null || documentaryCheck != null -> "reality-check"
                else -> "match"
            },
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
                    .distinctUntilChangedBy { it?.ids?.trakt to it?.isAnyVip }
                    .collect { user ->
                        userState.update { user }
                        keepMatchForAccount(user)
                        sentimentState.update {
                            when {
                                sentiments == null -> null
                                user?.isAnyVip == true -> sentiments
                                else -> getMockSentimentUseCase.getMockSentiments()
                            }
                        }
                        if (user?.isAnyVip == true) {
                            loadMatchReasons()
                        }
                    }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun keepMatchForAccount(user: User?) {
        if (matchState.value == null) return

        val owner = matchOwnerId
        val userId = user?.ids?.trakt
        if (owner == null && userId == null) return
        if (userId != null && (owner == null || owner == userId)) {
            matchOwnerId = userId
            return
        }

        matchState.update { null }
        matchReasonsState.update { EmptyImmutableList }
        matchReasonsLoadingState.update { LoadingState.Idle }
        tabState.update { tab ->
            tab.takeUnless { it == InsightsTab.Match } ?: tabs.firstOrNull { it != InsightsTab.Match } ?: tab
        }
    }

    private fun loadMatchReasons() {
        val match = matchState.value
        if (match == null || match.isLow) return
        if (matchReasonsLoadingState.value != LoadingState.Idle) return

        viewModelScope.launch {
            try {
                matchReasonsLoadingState.update { Loading }
                matchReasonsState.update {
                    getMatchReasonsUseCase.getReasons(
                        mediaId = TraktId(destination.mediaId),
                        mediaType = destination.mediaType,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                matchReasonsLoadingState.update { Done }
            }
        }
    }

    fun selectTab(tab: InsightsTab) {
        if (tabState.value == tab) return

        tabState.update { tab }
        analytics.logInsightsTab(tab.value)
    }

    fun setSectionCollapsed(
        section: RealityCheckSection,
        collapsed: Boolean,
    ) {
        sectionCollapses.getValue(section).set(collapsed)
    }

    fun onVipUpsellSeen() {
        analytics.vip.logUpsellImpression(tabState.value.upsellSource.value)
    }

    val state = combine(
        userState,
        sentimentState,
        tabState,
        collapsedSectionsState,
        matchReasonsState,
        matchReasonsLoadingState,
        matchState,
    ) { state ->
        SentimentState(
            user = state[0] as? User,
            sentiments = state[1] as Sentiments?,
            realityCheck = initialState.realityCheck,
            documentaryCheck = initialState.documentaryCheck,
            match = state[6] as MediaMatch?,
            matchReasons = state[4] as ImmutableList<RecommendedSource>,
            matchReasonsLoading = state[5] as LoadingState,
            tabs = initialState.tabs
                .filter { it != InsightsTab.Match || state[6] != null }
                .toImmutableList(),
            tab = state[2] as InsightsTab,
            collapsedSections = state[3] as ImmutableSet<RealityCheckSection>,
            backgroundUrl = initialState.backgroundUrl,
            mediaTitle = initialState.mediaTitle,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
