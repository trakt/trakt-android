package tv.trakt.trakt.core.userprofile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType
import tv.trakt.trakt.core.userprofile.UserProfileScreen

@Serializable
internal data class UserProfileDestination(
    val userJson: String,
)

internal fun NavGraphBuilder.userProfileScreen(
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToUser: (User) -> Unit,
    onNavigateToList: (CustomList) -> Unit,
    onNavigateToAllHistory: (TraktId) -> Unit,
    onNavigateToAllFavorites: (TraktId) -> Unit,
    onNavigateToAllLists: (User, PersonalListType) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<UserProfileDestination> {
        UserProfileScreen(
            viewModel = koinViewModel(),
            onNavigateToShow = onNavigateToShow,
            onNavigateToMovie = onNavigateToMovie,
            onNavigateToEpisode = onNavigateToEpisode,
            onNavigateToUser = onNavigateToUser,
            onNavigateToList = onNavigateToList,
            onNavigateToAllHistory = onNavigateToAllHistory,
            onNavigateToAllFavorites = onNavigateToAllFavorites,
            onNavigateToAllLists = onNavigateToAllLists,
            onNavigateBack = onNavigateBack,
        )
    }
}

internal fun NavController.navigateToUserProfile(
    user: User,
    currentUserId: TraktId?,
) {
    if (user.ids.trakt == currentUserId) {
        return
    }
    navigate(route = UserProfileDestination(userJson = Json.encodeToString(user)))
}
