package tv.trakt.trakt.tv.core.details.lists.details.movies.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.tv.core.details.lists.details.movies.CustomListMoviesViewModel
import tv.trakt.trakt.tv.core.details.lists.details.movies.usecases.GetListItemsUseCase

internal val customListMoviesModule = module {

    factory {
        GetListItemsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    viewModel {
        CustomListMoviesViewModel(
            savedStateHandle = get(),
            getListItemsUseCase = get(),
        )
    }
}
