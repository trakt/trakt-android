package tv.trakt.trakt.core.people.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.Config.API_HD_BASE_URL
import tv.trakt.trakt.core.people.data.local.PeopleLocalDataSource
import tv.trakt.trakt.core.people.data.local.PeopleStorage
import tv.trakt.trakt.core.people.data.remote.PeopleApiClient
import tv.trakt.trakt.core.people.data.remote.PeopleRemoteDataSource
import tv.trakt.trakt.core.people.data.remote.api.PeopleExtrasApi
import tv.trakt.trakt.core.people.usecases.GetPersonUseCase

internal val peopleDataModule = module {
    single<PeopleRemoteDataSource> {
        PeopleApiClient(
            peopleExtrasApi = PeopleExtrasApi(
                baseUrl = API_HD_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
            peopleApi = get(),
        )
    }

    single<PeopleLocalDataSource> {
        PeopleStorage()
    }
}

internal val peopleModule = module {
    factory {
        GetPersonUseCase(
            peopleLocalSource = get(),
            peopleRemoteSource = get(),
        )
    }
}
