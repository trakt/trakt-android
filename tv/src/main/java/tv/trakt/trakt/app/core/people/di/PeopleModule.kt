package tv.trakt.trakt.app.core.people.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.people.PersonDetailsViewModel
import tv.trakt.trakt.app.core.people.usecases.GetPersonCreditsUseCase
import tv.trakt.trakt.app.core.people.usecases.GetPersonUseCase

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
            movieLocalSource = get(),
        )
    }

    viewModel {
        PersonDetailsViewModel(
            savedStateHandle = get(),
            getPersonUseCase = get(),
            getPersonCreditsUseCase = get(),
            collectionStateProvider = get(),
        )
    }
}
