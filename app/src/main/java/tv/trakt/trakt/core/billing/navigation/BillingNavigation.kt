package tv.trakt.trakt.core.billing.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.core.billing.BillingScreen

@Serializable
internal data object BillingDestination

internal fun NavGraphBuilder.billingScreen(onNavigateBack: () -> Unit) {
    composable<BillingDestination> {
        BillingScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToBilling() {
    navigate(route = BillingDestination)
}
