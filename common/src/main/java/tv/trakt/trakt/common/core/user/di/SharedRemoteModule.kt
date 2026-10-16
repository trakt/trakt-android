package tv.trakt.trakt.common.core.user.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.core.comments.data.remote.CommentsApiClient
import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteDataSource
import tv.trakt.trakt.common.core.lists.data.remote.ListsApiClient
import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.core.search.data.remote.SearchApiClient
import tv.trakt.trakt.common.core.search.data.remote.SearchRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.UserApiClient
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.calendar.UserCalendarApiClient
import tv.trakt.trakt.common.core.user.data.remote.calendar.UserCalendarRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.favorites.UserFavoritesApiClient
import tv.trakt.trakt.common.core.user.data.remote.favorites.UserFavoritesRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryApiClient
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.otherlists.UserOtherListsApiClient
import tv.trakt.trakt.common.core.user.data.remote.otherlists.UserOtherListsRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.personallists.UserPersonalListsApiClient
import tv.trakt.trakt.common.core.user.data.remote.personallists.UserPersonalListsRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.ratings.UserRatingsApiClient
import tv.trakt.trakt.common.core.user.data.remote.ratings.UserRatingsRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.smartlists.UserSmartListsApiClient
import tv.trakt.trakt.common.core.user.data.remote.smartlists.UserSmartListsRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialApiClient
import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistApiClient
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistRemoteDataSource

val userRemoteModule = module {
    singleOf(::UserApiClient) { bind<UserRemoteDataSource>() }
    singleOf(::UserHistoryApiClient) { bind<UserHistoryRemoteDataSource>() }
    singleOf(::UserWatchlistApiClient) { bind<UserWatchlistRemoteDataSource>() }
    singleOf(::UserFavoritesApiClient) { bind<UserFavoritesRemoteDataSource>() }
    singleOf(::UserOtherListsApiClient) { bind<UserOtherListsRemoteDataSource>() }
    singleOf(::UserRatingsApiClient) { bind<UserRatingsRemoteDataSource>() }
    singleOf(::UserPersonalListsApiClient) { bind<UserPersonalListsRemoteDataSource>() }
    singleOf(::UserSmartListsApiClient) { bind<UserSmartListsRemoteDataSource>() }
    singleOf(::UserSocialApiClient) { bind<UserSocialRemoteDataSource>() }

    single<UserCalendarRemoteDataSource> {
        UserCalendarApiClient(
            calendarsApi = get(named("authorizedCalendarsApi")),
        )
    }
}

val commentsRemoteModule = module {
    single<CommentsRemoteDataSource> {
        CommentsApiClient(
            api = get(),
            authorizedApi = get(named("authorizedCommentsApi")),
            cacheMarker = get(),
        )
    }
}

val searchRemoteModule = module {
    single<SearchRemoteDataSource> {
        SearchApiClient(
            api = get(),
            authorizedApi = get(named("authorizedSearchApi")),
        )
    }
}

val listsRemoteModule = module {
    singleOf(::ListsApiClient) { bind<ListsRemoteDataSource>() }
}
