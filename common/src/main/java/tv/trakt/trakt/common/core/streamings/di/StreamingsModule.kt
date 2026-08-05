package tv.trakt.trakt.common.core.streamings.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.streamings.data.local.StreamingLocalDataSource
import tv.trakt.trakt.common.core.streamings.data.local.StreamingStorage
import tv.trakt.trakt.common.core.streamings.data.remote.StreamingApiClient
import tv.trakt.trakt.common.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.common.core.streamings.helpers.PriorityStreamingServiceProvider
import tv.trakt.trakt.common.core.streamings.usecase.GetAllStreamingsUseCase
import tv.trakt.trakt.common.core.streamings.usecase.GetPriorityStreamingUseCase
import tv.trakt.trakt.common.core.streamings.usecase.GetStreamingsUseCase

val streamingsDataModule = module {
    singleOf(::StreamingApiClient) { bind<StreamingRemoteDataSource>() }
    singleOf(::StreamingStorage) { bind<StreamingLocalDataSource>() }
    singleOf(::PriorityStreamingServiceProvider)
}

val streamingsModule = module {
    factoryOf(::GetStreamingsUseCase)
    factoryOf(::GetAllStreamingsUseCase)
    factoryOf(::GetPriorityStreamingUseCase)
}
