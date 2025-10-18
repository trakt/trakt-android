package tv.trakt.trakt.core.summary.people.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.summary.people.PersonDetailsViewModel

internal val personDetailsModule = module {
    viewModel {
        PersonDetailsViewModel(
            savedStateHandle = get(),
            getPersonUseCase = get(),
            getPersonCreditsUseCase = get(),
        )
    }
}
