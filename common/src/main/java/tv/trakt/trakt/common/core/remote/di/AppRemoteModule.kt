package tv.trakt.trakt.common.core.remote.di

import io.ktor.client.HttpClientConfig
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.common.core.billing.data.remote.BillingApiClient
import tv.trakt.trakt.common.core.billing.data.remote.BillingRemoteDataSource
import tv.trakt.trakt.common.core.checkin.data.remote.CheckInApiClient
import tv.trakt.trakt.common.core.checkin.data.remote.CheckInRemoteDataSource
import tv.trakt.trakt.common.core.parentalguide.data.remote.ParentalGuideApiClient
import tv.trakt.trakt.common.core.parentalguide.data.remote.ParentalGuideRemoteDataSource
import tv.trakt.trakt.common.core.ratings.data.remote.RatingsApiClient
import tv.trakt.trakt.common.core.ratings.data.remote.RatingsRemoteDataSource
import tv.trakt.trakt.common.core.reactions.data.remote.ReactionsApiClient
import tv.trakt.trakt.common.core.reactions.data.remote.ReactionsRemoteDataSource
import tv.trakt.trakt.common.core.reactions.media.data.remote.MediaReactionsApiClient
import tv.trakt.trakt.common.core.reactions.media.data.remote.MediaReactionsRemoteDataSource
import tv.trakt.trakt.common.core.today.data.remote.TodayApiClient
import tv.trakt.trakt.common.core.today.data.remote.TodayRemoteDataSource
import tv.trakt.trakt.common.core.younify.data.remote.YounifyApiClient
import tv.trakt.trakt.common.core.younify.data.remote.YounifyRemoteDataSource

val billingRemoteModule = module {
    single<BillingRemoteDataSource> {
        BillingApiClient(
            baseUrl = API_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("clientConfig")),
        )
    }
}

val checkInRemoteModule = module {
    singleOf(::CheckInApiClient) { bind<CheckInRemoteDataSource>() }
}

val parentalGuideRemoteModule = module {
    singleOf(::ParentalGuideApiClient) { bind<ParentalGuideRemoteDataSource>() }
}

val ratingsRemoteModule = module {
    singleOf(::RatingsApiClient) { bind<RatingsRemoteDataSource>() }
}

val reactionsRemoteModule = module {
    singleOf(::ReactionsApiClient) { bind<ReactionsRemoteDataSource>() }
    singleOf(::MediaReactionsApiClient) { bind<MediaReactionsRemoteDataSource>() }
}

val todayRemoteModule = module {
    singleOf(::TodayApiClient) { bind<TodayRemoteDataSource>() }
}

val younifyRemoteModule = module {
    single<YounifyRemoteDataSource> {
        YounifyApiClient(
            baseUrl = API_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")),
        )
    }
}
