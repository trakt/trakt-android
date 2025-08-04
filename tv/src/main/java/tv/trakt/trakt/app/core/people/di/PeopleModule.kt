package tv.trakt.trakt.app.core.people.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.PeopleApi
import tv.trakt.trakt.app.Config.API_BASE_URL
import tv.trakt.trakt.app.core.people.PersonDetailsViewModel
import tv.trakt.trakt.app.core.people.data.local.PeopleLocalDataSource
import tv.trakt.trakt.app.core.people.data.local.PeopleStorage
import tv.trakt.trakt.app.core.people.data.remote.PeopleApiClient
import tv.trakt.trakt.app.core.people.data.remote.PeopleRemoteDataSource
import tv.trakt.trakt.app.core.people.usecases.GetPersonCreditsUseCase
import tv.trakt.trakt.app.core.people.usecases.GetPersonUseCase

internal val peopleDataModule = module {
    single<PeopleLocalDataSource> {
        PeopleStorage()
    }

    single<PeopleRemoteDataSource> {
        PeopleApiClient(
            api = PeopleApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
        )
    }
}

internal val personDetailsModule = module {

    factory {
        GetPersonUseCase(
            peopleLocalSource = get(),
            peopleRemoteSource = get(),
        )
    }

    factory {
        GetPersonCreditsUseCase(
            peopleRemoteSource = get(),
            showLocalSource = get(),
            movieLocalDataSource = get(),
        )
    }

    viewModel {
        PersonDetailsViewModel(
            savedStateHandle = get(),
            getPersonUseCase = get(),
            getPersonCreditsUseCase = get(),
        )
    }
}
