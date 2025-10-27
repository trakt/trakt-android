package tv.trakt.trakt.core.user.usecases

import android.content.Context
import androidx.work.WorkManager
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalLocalDataSource
import tv.trakt.trakt.core.home.sections.activity.data.local.social.HomeSocialLocalDataSource
import tv.trakt.trakt.core.home.sections.upcoming.data.local.HomeUpcomingLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.movies.sections.recommended.data.local.RecommendedMoviesLocalDataSource
import tv.trakt.trakt.core.search.data.local.RecentSearchLocalDataSource
import tv.trakt.trakt.core.shows.sections.recommended.data.local.RecommendedShowsLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserListsLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.reactions.UserReactionsLocalDataSource

internal class LogoutUserUseCase(
    private val appContext: Context,
    private val sessionManager: SessionManager,
    private val apiClients: Array<ApiClient>,
    private val localUpNext: HomeUpNextLocalDataSource,
    private val localUpcoming: HomeUpcomingLocalDataSource,
    private val localSocial: HomeSocialLocalDataSource,
    private val localPersonal: HomePersonalLocalDataSource,
    private val localRecentSearch: RecentSearchLocalDataSource,
    private val localListsPersonal: ListsPersonalLocalDataSource,
    private val localListsItemsPersonal: ListsPersonalItemsLocalDataSource,
    private val localRecommendedShows: RecommendedShowsLocalDataSource,
    private val localRecommendedMovies: RecommendedMoviesLocalDataSource,
    private val localUserProgress: UserProgressLocalDataSource,
    private val localUserWatchlist: UserWatchlistLocalDataSource,
    private val localUserLists: UserListsLocalDataSource,
    private val localUserReactions: UserReactionsLocalDataSource,
) {
    suspend fun logoutUser() {
        sessionManager.clear()
        apiClients.forEach { api ->
            api.client.authProvider<BearerAuthProvider>()?.clearToken()
        }

        localUpNext.clear()
        localUpcoming.clear()
        localSocial.clear()
        localPersonal.clear()
        localRecentSearch.clear()
        localListsPersonal.clear()
        localListsItemsPersonal.clear()
        localUserProgress.clear()
        localUserWatchlist.clear()
        localUserLists.clear()
        localUserReactions.clear()

        localRecommendedShows.clear()
        localRecommendedMovies.clear()

        WorkManager.getInstance(appContext).cancelAllWork()
    }
}
