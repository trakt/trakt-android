package tv.trakt.trakt.core.summary.sentiment.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Sentiments
import tv.trakt.trakt.core.summary.sentiment.SentimentScreen
import tv.trakt.trakt.core.summary.sentiment.SentimentViewModel

@Serializable
internal data class SentimentDestination(
    val sentimentsDto: String,
    val mediaTitle: String?,
    val mediaImage: String?,
    val navSource: String,
)

internal fun NavGraphBuilder.sentimentScreen(
    onNavigateVip: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<SentimentDestination> {
        SentimentScreen(
            viewModel = koinViewModel<SentimentViewModel>(),
            onNavigateVip = onNavigateVip,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToSentiment(
    sentiments: Sentiments,
    mediaTitle: String?,
    mediaImage: String?,
    navSource: String,
) {
    navigate(
        route = SentimentDestination(
            sentimentsDto = Json.encodeToString(sentiments),
            mediaTitle = mediaTitle,
            mediaImage = mediaImage,
            navSource = navSource,
        ),
    )
}
