package tv.trakt.trakt.core.people.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.core.people.data.local.PeopleLocalDataSource
import tv.trakt.trakt.core.people.data.local.PeopleStorage
import tv.trakt.trakt.core.people.usecases.GetPersonCreditsUseCase
import tv.trakt.trakt.core.people.usecases.GetPersonUseCase

internal val peopleDataModule = module {
    singleOf(::PeopleStorage) { bind<PeopleLocalDataSource>() }
}

internal val peopleModule = module {
    factoryOf(::GetPersonUseCase)
    factoryOf(::GetPersonCreditsUseCase)
}
