package tv.trakt.trakt.core.summary.credits.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.summary.credits.AllCreditsViewModel
import tv.trakt.trakt.core.summary.credits.usecases.GetMediaCreditsUseCase

internal val allCreditsModule = module {
    factoryOf(::GetMediaCreditsUseCase)

    viewModelOf(::AllCreditsViewModel)
}
