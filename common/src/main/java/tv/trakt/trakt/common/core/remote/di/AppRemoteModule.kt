package tv.trakt.trakt.common.core.remote.di

import io.ktor.client.HttpClientConfig
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.common.core.billing.data.remote.BillingRemoteSource
import tv.trakt.trakt.common.core.checkin.data.remote.CheckInRemoteSource
import tv.trakt.trakt.common.core.parentalguide.data.remote.ParentalGuideRemoteSource
import tv.trakt.trakt.common.core.ratings.data.remote.RatingsRemoteSource
import tv.trakt.trakt.common.core.reactions.data.remote.ReactionsRemoteSource
import tv.trakt.trakt.common.core.reactions.media.data.remote.MediaReactionsRemoteSource
import tv.trakt.trakt.common.core.today.data.remote.TodayRemoteSource
import tv.trakt.trakt.common.core.younify.data.remote.YounifyRemoteSource

val billingRemoteModule = module {
    single {
        BillingRemoteSource(
            baseUrl = API_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("clientConfig")),
        )
    }
}

val checkInRemoteModule = module {
    singleOf(::CheckInRemoteSource)
}

val parentalGuideRemoteModule = module {
    singleOf(::ParentalGuideRemoteSource)
}

val ratingsRemoteModule = module {
    singleOf(::RatingsRemoteSource)
}

val reactionsRemoteModule = module {
    singleOf(::ReactionsRemoteSource)
    singleOf(::MediaReactionsRemoteSource)
}

val todayRemoteModule = module {
    singleOf(::TodayRemoteSource)
}

val younifyRemoteModule = module {
    single {
        YounifyRemoteSource(
            baseUrl = API_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")),
        )
    }
}
