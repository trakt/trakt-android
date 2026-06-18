package tv.trakt.trakt.core.people.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.core.people.data.local.PeopleLocalDataSource
import tv.trakt.trakt.core.people.data.local.PeopleStorage
import tv.trakt.trakt.core.people.data.remote.PeopleApiClient
import tv.trakt.trakt.core.people.data.remote.PeopleRemoteDataSource
import tv.trakt.trakt.core.people.data.remote.api.PeopleExtrasApi
import tv.trakt.trakt.core.people.usecases.GetPersonCreditsUseCase
import tv.trakt.trakt.core.people.usecases.GetPersonUseCase

internal val peopleDataModule = module {
    single<PeopleRemoteDataSource> {
        PeopleApiClient(
            peopleExtrasApi = PeopleExtrasApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
            peopleApi = get(),
        )
    }

    singleOf(::PeopleStorage) { bind<PeopleLocalDataSource>() }
}

internal val peopleModule = module {
    factoryOf(::GetPersonUseCase)
    factoryOf(::GetPersonCreditsUseCase)
}
