package tv.trakt.trakt.common.core.user.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteSource
import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.core.search.data.remote.SearchRemoteSource
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteSource
import tv.trakt.trakt.common.core.user.data.remote.calendar.UserCalendarRemoteSource
import tv.trakt.trakt.common.core.user.data.remote.favorites.UserFavoritesRemoteSource
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteSource
import tv.trakt.trakt.common.core.user.data.remote.otherlists.UserOtherListsRemoteSource
import tv.trakt.trakt.common.core.user.data.remote.personallists.UserPersonalListsRemoteSource
import tv.trakt.trakt.common.core.user.data.remote.ratings.UserRatingsRemoteSource
import tv.trakt.trakt.common.core.user.data.remote.smartlists.UserSmartListsRemoteSource
import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialRemoteSource
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistRemoteSource

val userRemoteModule = module {
    singleOf(::UserRemoteSource)
    singleOf(::UserHistoryRemoteSource)
    singleOf(::UserWatchlistRemoteSource)
    singleOf(::UserFavoritesRemoteSource)
    singleOf(::UserOtherListsRemoteSource)
    singleOf(::UserRatingsRemoteSource)
    singleOf(::UserPersonalListsRemoteSource)
    singleOf(::UserSmartListsRemoteSource)
    singleOf(::UserSocialRemoteSource)

    single {
        UserCalendarRemoteSource(
            calendarsApi = get(named("authorizedCalendarsApi")),
        )
    }
}

val commentsRemoteModule = module {
    single {
        CommentsRemoteSource(
            api = get(),
            authorizedApi = get(named("authorizedCommentsApi")),
            cacheMarker = get(),
        )
    }
}

val searchRemoteModule = module {
    single {
        SearchRemoteSource(
            api = get(),
            authorizedApi = get(named("authorizedSearchApi")),
        )
    }
}

val listsRemoteModule = module {
    singleOf(::ListsRemoteSource)
}
