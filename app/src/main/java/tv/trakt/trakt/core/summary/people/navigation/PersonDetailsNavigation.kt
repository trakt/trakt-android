package tv.trakt.trakt.core.summary.people.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.people.PersonDetailsScreen

@Serializable
internal data class PersonDestination(
    val personId: Int,
    val sourceMediaId: Int?,
    val backdropUrl: String?,
)

internal fun NavGraphBuilder.personDetailsScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<PersonDestination> {
        PersonDetailsScreen(
            viewModel = koinViewModel(),
            onNavigateBack = onNavigateBack,
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
        )
    }
}

internal fun NavController.navigateToPerson(
    personId: TraktId,
    sourceMediaId: TraktId?,
    backdropUrl: String?,
) {
    if (personId.value <= 0) {
        Timber.d("Invalid personId: ${personId.value}")
        return
    }

    navigate(
        route = PersonDestination(
            personId = personId.value,
            sourceMediaId = sourceMediaId?.value,
            backdropUrl = backdropUrl,
        ),
    )
}
