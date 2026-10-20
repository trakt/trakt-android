package tv.trakt.trakt.core.people.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tv.trakt.trakt.core.people.usecases.GetPersonCreditsUseCase
import tv.trakt.trakt.core.people.usecases.GetPersonUseCase

internal val peopleModule = module {
    factoryOf(::GetPersonUseCase)
    factoryOf(::GetPersonCreditsUseCase)
}
