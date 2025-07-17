package tv.trakt.trakt.tv.helpers.extensions

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

fun NavOptionsBuilder.popUpToTop(navController: NavController) {
    popUpTo(navController.graph.id) {
        inclusive = true
    }
    launchSingleTop = true
}
