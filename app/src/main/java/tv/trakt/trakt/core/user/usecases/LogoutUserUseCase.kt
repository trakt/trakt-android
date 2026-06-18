package tv.trakt.trakt.core.user.usecases

import android.content.Context
import androidx.work.WorkManager
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.data.local.liked.UserLikedListsLocalDataSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.inappreview.RequestAppReviewUseCase
import tv.trakt.trakt.core.billing.data.remote.BillingRemoteDataSource
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates
import tv.trakt.trakt.core.discover.sections.recommended.data.local.movies.RecommendedMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.recommended.data.local.shows.RecommendedShowsLocalDataSource
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalLocalDataSource
import tv.trakt.trakt.core.home.sections.activity.data.local.social.HomeSocialLocalDataSource
import tv.trakt.trakt.core.home.sections.upcoming.data.local.HomeUpcomingLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.home.sections.watchlist.data.local.HomeWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.items.ListsCollaborationsItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.lists.ListsCollaborationsLocalDataSource
import tv.trakt.trakt.core.lists.sections.liked.data.local.items.ListsLikedItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.liked.data.local.lists.ListsLikedLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.notifications.data.work.ScheduleNotificationsWorker
import tv.trakt.trakt.core.profile.sections.activity.data.local.comments.ProfileCommentsLocalDataSource
import tv.trakt.trakt.core.profile.sections.activity.data.local.ratings.ProfileRatingsLocalDataSource
import tv.trakt.trakt.core.profile.sections.progress.data.local.completed.ProgressCompletedLocalDataSource
import tv.trakt.trakt.core.profile.sections.progress.data.local.dropped.ProgressDroppedLocalDataSource
import tv.trakt.trakt.core.profile.sections.progress.data.local.watching.ProgressWatchingLocalDataSource
import tv.trakt.trakt.core.profile.sections.screentime.data.local.ScreenTimeLocalDataSource
import tv.trakt.trakt.core.settings.features.younify.data.remote.YounifyRemoteDataSource
import tv.trakt.trakt.core.user.data.local.UserListsLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.core.user.data.local.favorites.UserFavoritesLocalDataSource
import tv.trakt.trakt.core.user.data.local.library.UserLibraryLocalDataSource
import tv.trakt.trakt.core.user.data.local.ratings.UserRatingsLocalDataSource
import tv.trakt.trakt.core.user.data.local.reactions.UserReactionsLocalDataSource
import tv.trakt.trakt.core.user.data.local.watchlist.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.watchlist.minimal.UserWatchlistMinimalLocalDataSource
import tv.trakt.trakt.helpers.collapsing.CollapsingManager

internal class LogoutUserUseCase(
    private val appContext: Context,
    private val sessionManager: SessionManager,
    private val collapsingManager: CollapsingManager,
    private val checkInManager: CheckInManager,
    private val apiClients: Array<ApiClient>,
    private val younifyApiClient: YounifyRemoteDataSource,
    private val billingApiClient: BillingRemoteDataSource,
    private val localUpNext: HomeUpNextLocalDataSource,
    private val localWatchlistUpNext: HomeWatchlistLocalDataSource,
    private val localUpcoming: HomeUpcomingLocalDataSource,
    private val localSocial: HomeSocialLocalDataSource,
    private val localPersonal: HomePersonalLocalDataSource,
    private val localListsPersonal: ListsPersonalLocalDataSource,
    private val localListsItemsPersonal: ListsPersonalItemsLocalDataSource,
    private val localListsLiked: ListsLikedLocalDataSource,
    private val localListsItemsLiked: ListsLikedItemsLocalDataSource,
    private val localListsCollab: ListsCollaborationsLocalDataSource,
    private val localListsCollabItems: ListsCollaborationsItemsLocalDataSource,
    private val localRecommendedShows: RecommendedShowsLocalDataSource,
    private val localRecommendedMovies: RecommendedMoviesLocalDataSource,
    private val localUserProgress: UserProgressLocalDataSource,
    private val localUserWatchlist: UserWatchlistLocalDataSource,
    private val localUserWatchlistMin: UserWatchlistMinimalLocalDataSource,
    private val localUserFavorites: UserFavoritesLocalDataSource,
    private val localUserLibrary: UserLibraryLocalDataSource,
    private val localUserRatings: UserRatingsLocalDataSource,
    private val localUserLists: UserListsLocalDataSource,
    private val localUserLikedLists: UserLikedListsLocalDataSource,
    private val localUserReactions: UserReactionsLocalDataSource,
    private val localProfileCompleted: ProgressCompletedLocalDataSource,
    private val localProfileWatching: ProgressWatchingLocalDataSource,
    private val localProfileDropped: ProgressDroppedLocalDataSource,
    private val localScreenTime: ScreenTimeLocalDataSource,
    private val localProfileRatings: ProfileRatingsLocalDataSource,
    private val localProfileComments: ProfileCommentsLocalDataSource,
    private val appReviewUseCase: RequestAppReviewUseCase,
    private val analytics: Analytics,
) {
    suspend fun logoutUser() {
        sessionManager.clear()
        collapsingManager.clear()
        checkInManager.stop(
            source = CheckInUpdates.Source.Default,
            context = appContext,
        )

        apiClients
            .forEach { api ->
                api.client.authProvider<BearerAuthProvider>()?.clearToken()
            }.also {
                younifyApiClient.clear()
                billingApiClient.clear()
            }

        localUpNext.clear()
        localWatchlistUpNext.clear()
        localUpcoming.clear()
        localSocial.clear()
        localPersonal.clear()
        localListsPersonal.clear()
        localListsItemsPersonal.clear()
        localListsLiked.clear()
        localListsItemsLiked.clear()
        localListsCollab.clear()
        localListsCollabItems.clear()
        localUserProgress.clear()
        localUserWatchlist.clear()
        localUserWatchlistMin.clear()
        localUserLists.clear()
        localUserLikedLists.clear()
        localUserFavorites.clear()
        localUserLibrary.clear()
        localUserReactions.clear()
        localUserRatings.clear()
        localProfileCompleted.clear()
        localProfileWatching.clear()
        localProfileDropped.clear()
        localScreenTime.clear()
        localProfileRatings.clear()
        localProfileComments.clear()

        localRecommendedShows.clear()
        localRecommendedMovies.clear()

        appReviewUseCase.clear()
        analytics.setUserId(null)
        ScheduleNotificationsWorker.clear(appContext)
        WorkManager.getInstance(appContext).cancelAllWork()
    }
}
