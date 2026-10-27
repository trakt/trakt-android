package tv.trakt.trakt.common.core.streamings.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.streamings.helpers.PriorityStreamingServiceProvider
import tv.trakt.trakt.common.core.streamings.usecase.GetAllStreamingsUseCase
import tv.trakt.trakt.common.core.streamings.usecase.GetPriorityStreamingUseCase
import tv.trakt.trakt.common.core.streamings.usecase.GetStreamingsUseCase
import tv.trakt.trakt.common.core.streamings.usecase.GetYouTubeSpecialUseCase

val streamingsDataModule = module {
    singleOf(::PriorityStreamingServiceProvider)
}

val streamingsModule = module {
    factoryOf(::GetStreamingsUseCase)
    factoryOf(::GetAllStreamingsUseCase)
    factoryOf(::GetPriorityStreamingUseCase)
    factoryOf(::GetYouTubeSpecialUseCase)
}
