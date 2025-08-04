package tv.trakt.trakt.app.core.details.lists.details.shows.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.details.lists.details.shows.CustomListShowsViewModel
import tv.trakt.trakt.app.core.details.lists.details.shows.usecases.GetListItemsUseCase

internal val customListShowsModule = module {

    factory {
        GetListItemsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    viewModel {
        CustomListShowsViewModel(
            savedStateHandle = get(),
            getListItemsUseCase = get(),
        )
    }
}
