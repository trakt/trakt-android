package tv.trakt.trakt.core.profile.usecase

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalLocalDataSource
import tv.trakt.trakt.core.home.sections.activity.data.local.social.HomeSocialLocalDataSource
import tv.trakt.trakt.core.home.sections.upcoming.data.local.HomeUpcomingLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.home.sections.watchlist.data.local.HomeWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.lists.sections.watchlist.data.local.ListsWatchlistLocalDataSource
import tv.trakt.trakt.core.search.data.local.RecentSearchLocalDataSource

internal class LogoutUserUseCase(
    private val sessionManager: SessionManager,
    private val localUpNext: HomeUpNextLocalDataSource,
    private val localWatchlist: HomeWatchlistLocalDataSource,
    private val localUpcoming: HomeUpcomingLocalDataSource,
    private val localSocial: HomeSocialLocalDataSource,
    private val localPersonal: HomePersonalLocalDataSource,
    private val localRecentSearch: RecentSearchLocalDataSource,
    private val localListsPersonal: ListsPersonalLocalDataSource,
    private val localListsItemsPersonal: ListsPersonalItemsLocalDataSource,
    private val localListsWatchlist: ListsWatchlistLocalDataSource,
    private val localListsShowsWatchlist: ListsWatchlistLocalDataSource,
    private val localListsMoviesWatchlist: ListsWatchlistLocalDataSource,
) {
    suspend fun logoutUser() {
        sessionManager.clear()
        localUpNext.clear()
        localWatchlist.clear()
        localUpcoming.clear()
        localSocial.clear()
        localPersonal.clear()
        localRecentSearch.clear()
        localListsPersonal.clear()
        localListsItemsPersonal.clear()
        localListsWatchlist.clear()
        localListsShowsWatchlist.clear()
        localListsMoviesWatchlist.clear()
    }
}
