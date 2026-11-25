package tv.trakt.trakt.core.vipveteran.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tv.trakt.trakt.core.vipveteran.usecases.ObserveUserTierStateUseCase

internal val vipVeteranModule = module {
    factoryOf(::ObserveUserTierStateUseCase)
}
