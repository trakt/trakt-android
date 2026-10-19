package tv.trakt.trakt.common.data.remote.di

import io.ktor.client.HttpClientConfig
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.common.Config.WEB_AUTH_URL
import tv.trakt.trakt.common.data.remote.AuthRemoteSource
import tv.trakt.trakt.common.data.remote.BillingRemoteSource
import tv.trakt.trakt.common.data.remote.CheckInRemoteSource
import tv.trakt.trakt.common.data.remote.CommentsRemoteSource
import tv.trakt.trakt.common.data.remote.EpisodesRemoteSource
import tv.trakt.trakt.common.data.remote.EpisodesSyncRemoteSource
import tv.trakt.trakt.common.data.remote.GifsRemoteSource
import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.data.remote.MediaReactionsRemoteSource
import tv.trakt.trakt.common.data.remote.MediaRemoteSource
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.data.remote.MoviesSyncRemoteSource
import tv.trakt.trakt.common.data.remote.ParentalGuideRemoteSource
import tv.trakt.trakt.common.data.remote.PeopleRemoteSource
import tv.trakt.trakt.common.data.remote.PlexRemoteSource
import tv.trakt.trakt.common.data.remote.RatingsRemoteSource
import tv.trakt.trakt.common.data.remote.ReactionsRemoteSource
import tv.trakt.trakt.common.data.remote.SearchRemoteSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.data.remote.ShowsSyncRemoteSource
import tv.trakt.trakt.common.data.remote.StreamingRemoteSource
import tv.trakt.trakt.common.data.remote.TodayRemoteSource
import tv.trakt.trakt.common.data.remote.TranslationsRemoteSource
import tv.trakt.trakt.common.data.remote.UserCalendarRemoteSource
import tv.trakt.trakt.common.data.remote.UserFavoritesRemoteSource
import tv.trakt.trakt.common.data.remote.UserHistoryRemoteSource
import tv.trakt.trakt.common.data.remote.UserOtherListsRemoteSource
import tv.trakt.trakt.common.data.remote.UserPersonalListsRemoteSource
import tv.trakt.trakt.common.data.remote.UserRatingsRemoteSource
import tv.trakt.trakt.common.data.remote.UserRemoteSource
import tv.trakt.trakt.common.data.remote.UserSmartListsRemoteSource
import tv.trakt.trakt.common.data.remote.UserSocialRemoteSource
import tv.trakt.trakt.common.data.remote.UserWatchlistRemoteSource
import tv.trakt.trakt.common.data.remote.YounifyRemoteSource

val remoteSourcesModule = module {
    singleOf(::CheckInRemoteSource)
    singleOf(::EpisodesRemoteSource)
    singleOf(::EpisodesSyncRemoteSource)
    singleOf(::GifsRemoteSource)
    singleOf(::ListsRemoteSource)
    singleOf(::MediaReactionsRemoteSource)
    singleOf(::MediaRemoteSource)
    singleOf(::MoviesRemoteSource)
    singleOf(::MoviesSyncRemoteSource)
    singleOf(::ParentalGuideRemoteSource)
    singleOf(::PeopleRemoteSource)
    singleOf(::PlexRemoteSource)
    singleOf(::RatingsRemoteSource)
    singleOf(::ReactionsRemoteSource)
    singleOf(::ShowsRemoteSource)
    singleOf(::ShowsSyncRemoteSource)
    singleOf(::StreamingRemoteSource)
    singleOf(::TodayRemoteSource)
    singleOf(::TranslationsRemoteSource)
    singleOf(::UserFavoritesRemoteSource)
    singleOf(::UserHistoryRemoteSource)
    singleOf(::UserOtherListsRemoteSource)
    singleOf(::UserPersonalListsRemoteSource)
    singleOf(::UserRatingsRemoteSource)
    singleOf(::UserRemoteSource)
    singleOf(::UserSmartListsRemoteSource)
    singleOf(::UserSocialRemoteSource)
    singleOf(::UserWatchlistRemoteSource)

    single {
        AuthRemoteSource(
            baseUrl = WEB_AUTH_URL,
            oauthApi = get(),
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("clientConfig")),
        )
    }
    single {
        BillingRemoteSource(
            baseUrl = API_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("clientConfig")),
        )
    }
    single {
        YounifyRemoteSource(
            baseUrl = API_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")),
        )
    }
    single {
        UserCalendarRemoteSource(
            calendarsApi = get(named("authorizedCalendarsApi")),
        )
    }
    single {
        CommentsRemoteSource(
            api = get(),
            authorizedApi = get(named("authorizedCommentsApi")),
            cacheMarker = get(),
        )
    }
    single {
        SearchRemoteSource(
            api = get(),
            authorizedApi = get(named("authorizedSearchApi")),
        )
    }
}
