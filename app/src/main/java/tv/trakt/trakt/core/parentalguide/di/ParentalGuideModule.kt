package tv.trakt.trakt.core.parentalguide.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.core.parentalguide.data.remote.ParentalGuideApiClient
import tv.trakt.trakt.core.parentalguide.data.remote.ParentalGuideRemoteDataSource

internal val parentalGuideDataModule = module {
    singleOf(::ParentalGuideApiClient) { bind<ParentalGuideRemoteDataSource>() }
}
