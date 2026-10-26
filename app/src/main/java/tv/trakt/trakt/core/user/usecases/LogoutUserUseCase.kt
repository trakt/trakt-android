package tv.trakt.trakt.core.user.usecases

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.work.WorkManager
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.auth.work.RevokeTokenWorker
import tv.trakt.trakt.common.data.local.HomeSocialLocalSource
import tv.trakt.trakt.common.data.local.HomeUpNextLocalSource
import tv.trakt.trakt.common.data.local.HomeUpcomingLocalSource
import tv.trakt.trakt.common.data.local.HomeWatchlistLocalSource
import tv.trakt.trakt.common.data.local.LeaderboardLocalSource
import tv.trakt.trakt.common.data.local.ListsCollaborationsLocalSource
import tv.trakt.trakt.common.data.local.ListsLikedLocalSource
import tv.trakt.trakt.common.data.local.ListsPersonalLocalSource
import tv.trakt.trakt.common.data.local.RecommendedMoviesLocalSource
import tv.trakt.trakt.common.data.local.RecommendedShowsLocalSource
import tv.trakt.trakt.common.data.local.TodaySeenLocalSource
import tv.trakt.trakt.common.data.local.UserFavoritesLocalSource
import tv.trakt.trakt.common.data.local.UserLibraryLocalSource
import tv.trakt.trakt.common.data.local.UserLikedListsLocalSource
import tv.trakt.trakt.common.data.local.UserListsLocalSource
import tv.trakt.trakt.common.data.local.UserMediaReactionsLocalSource
import tv.trakt.trakt.common.data.local.UserProgressLocalSource
import tv.trakt.trakt.common.data.local.UserRatingsLocalSource
import tv.trakt.trakt.common.data.local.UserReactionsLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
import tv.trakt.trakt.common.data.remote.BillingRemoteSource
import tv.trakt.trakt.common.data.remote.YounifyRemoteSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.inappreview.RequestAppReviewUseCase
import tv.trakt.trakt.core.auth.usecase.forceLoginKey
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalLocalSource
import tv.trakt.trakt.core.notifications.data.work.ScheduleNotificationsWorker
import tv.trakt.trakt.core.notifications.data.work.mir.ScheduleMonthReviewNotificationWorker
import tv.trakt.trakt.core.profile.sections.activity.data.local.comments.ProfileCommentsLocalSource
import tv.trakt.trakt.core.profile.sections.activity.data.local.ratings.ProfileRatingsLocalSource
import tv.trakt.trakt.core.profile.sections.progress.data.local.completed.ProgressCompletedLocalSource
import tv.trakt.trakt.core.profile.sections.progress.data.local.dropped.ProgressDroppedLocalSource
import tv.trakt.trakt.core.profile.sections.progress.data.local.watching.ProgressWatchingLocalSource
import tv.trakt.trakt.core.profile.sections.screentime.data.local.ProfileScreenTimeLocalSource
import tv.trakt.trakt.core.today.data.local.TodayLocalSource
import tv.trakt.trakt.helpers.collapsing.CollapsingManager

internal class LogoutUserUseCase(
    private val appContext: Context,
    private val sessionManager: SessionManager,
    private val tokenProvider: TokenProvider,
    private val authPreferences: DataStore<Preferences>,
    private val collapsingManager: CollapsingManager,
    private val checkInManager: CheckInManager,
    private val apiClients: Array<ApiClient>,
    private val younifyRemoteSource: YounifyRemoteSource,
    private val billingRemoteSource: BillingRemoteSource,
    private val localUpNext: HomeUpNextLocalSource,
    private val localWatchlistUpNext: HomeWatchlistLocalSource,
    private val localUpcoming: HomeUpcomingLocalSource,
    private val localSocial: HomeSocialLocalSource,
    private val localPersonal: HomePersonalLocalSource,
    private val localToday: TodayLocalSource,
    private val localTodaySeen: TodaySeenLocalSource,
    private val localListsPersonal: ListsPersonalLocalSource,
    private val localListsLiked: ListsLikedLocalSource,
    private val localListsCollab: ListsCollaborationsLocalSource,
    private val localRecommendedShows: RecommendedShowsLocalSource,
    private val localRecommendedMovies: RecommendedMoviesLocalSource,
    private val localUserProgress: UserProgressLocalSource,
    private val localUserWatchlist: UserWatchlistLocalSource,
    private val localUserWatchlistMin: UserWatchlistMinimalLocalSource,
    private val localUserFavorites: UserFavoritesLocalSource,
    private val localUserLibrary: UserLibraryLocalSource,
    private val localUserRatings: UserRatingsLocalSource,
    private val localUserLists: UserListsLocalSource,
    private val localUserLikedLists: UserLikedListsLocalSource,
    private val localUserReactions: UserReactionsLocalSource,
    private val localUserMediaReactions: UserMediaReactionsLocalSource,
    private val localProfileCompleted: ProgressCompletedLocalSource,
    private val localProfileWatching: ProgressWatchingLocalSource,
    private val localProfileDropped: ProgressDroppedLocalSource,
    private val localScreenTime: ProfileScreenTimeLocalSource,
    private val localLeaderboard: LeaderboardLocalSource,
    private val localProfileRatings: ProfileRatingsLocalSource,
    private val localProfileComments: ProfileCommentsLocalSource,
    private val appReviewUseCase: RequestAppReviewUseCase,
    private val analytics: Analytics,
) {
    suspend fun logoutUser() {
        val accessToken = tokenProvider.getToken()?.accessToken

        sessionManager.clear()
        authPreferences.edit { it[forceLoginKey] = true }
        collapsingManager.clear()
        checkInManager.stop(context = appContext)

        apiClients
            .forEach { api ->
                api.client.authProvider<BearerAuthProvider>()?.clearToken()
            }.also {
                younifyRemoteSource.clear()
                billingRemoteSource.clear()
            }

        localUpNext.clear()
        localWatchlistUpNext.clear()
        localUpcoming.clear()
        localSocial.clear()
        localPersonal.clear()
        localToday.clear()
        localTodaySeen.clear()
        localListsPersonal.clear()
        localListsLiked.clear()
        localListsCollab.clear()
        localUserProgress.clear()
        localUserWatchlist.clear()
        localUserWatchlistMin.clear()
        localUserLists.clear()
        localUserLikedLists.clear()
        localUserFavorites.clear()
        localUserLibrary.clear()
        localUserReactions.clear()
        localUserMediaReactions.clear()
        localUserRatings.clear()
        localProfileCompleted.clear()
        localProfileWatching.clear()
        localProfileDropped.clear()
        localScreenTime.clear()
        localLeaderboard.clear()
        localProfileRatings.clear()
        localProfileComments.clear()

        localRecommendedShows.clear()
        localRecommendedMovies.clear()

        appReviewUseCase.clear()

        ScheduleNotificationsWorker.clear(appContext)
        ScheduleMonthReviewNotificationWorker.clear(appContext)

        WorkManager.getInstance(appContext).cancelAllWork()

        if (!accessToken.isNullOrBlank()) {
            RevokeTokenWorker.schedule(appContext, accessToken)
        }

        analytics.setUserId(null)
        analytics.setUserProperty("vip", null)
        analytics.setUserProperty("director", null)
    }
}
