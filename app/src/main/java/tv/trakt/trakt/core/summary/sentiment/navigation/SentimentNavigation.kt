package tv.trakt.trakt.core.summary.sentiment.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.billing.model.VipUpsellSource
import tv.trakt.trakt.core.summary.sentiment.SentimentScreen
import tv.trakt.trakt.core.summary.sentiment.SentimentViewModel
import tv.trakt.trakt.core.summary.sentiment.model.InsightsTab
import tv.trakt.trakt.core.summary.sentiment.model.MediaInsights

@Serializable
internal data class SentimentDestination(
    val mediaId: Int,
    val sentimentsDto: String?,
    val realityCheckDto: String?,
    val tab: InsightsTab,
    val mediaTitle: String?,
    val mediaImage: String?,
    val navSource: String,
)

internal fun NavGraphBuilder.sentimentScreen(
    onNavigateVip: (source: VipUpsellSource) -> Unit,
    onNavigateToPerson: (SlugId, SentimentDestination) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<SentimentDestination> { entry ->
        val dest = entry.toRoute<SentimentDestination>()
        SentimentScreen(
            viewModel = koinViewModel<SentimentViewModel>(),
            onNavigateVip = onNavigateVip,
            onNavigateToPerson = { slug -> onNavigateToPerson(slug, dest) },
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToSentiment(
    mediaId: TraktId,
    insights: MediaInsights,
    mediaTitle: String?,
    mediaImage: String?,
    navSource: String,
) {
    navigate(
        route = SentimentDestination(
            mediaId = mediaId.value,
            sentimentsDto = insights.sentiments?.let { Json.encodeToString(it) },
            realityCheckDto = insights.realityCheck?.let { Json.encodeToString(it) },
            tab = insights.tab,
            mediaTitle = mediaTitle,
            mediaImage = mediaImage,
            navSource = navSource,
        ),
    )
}
