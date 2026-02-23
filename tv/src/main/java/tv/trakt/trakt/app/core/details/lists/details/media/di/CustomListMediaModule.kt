package tv.trakt.trakt.app.core.details.lists.details.media.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.details.lists.details.media.CustomListMediaViewModel
import tv.trakt.trakt.app.core.details.lists.details.media.usecases.GetListItemsUseCase

internal val customListMediaModule = module {

    factory {
        GetListItemsUseCase(
            remoteSource = get(),
            showLocalSource = get(),
            movieLocalSource = get(),
        )
    }

    viewModel {
        CustomListMediaViewModel(
            savedStateHandle = get(),
            getListItemsUseCase = get(),
            getUserLikedListsUseCase = get(),
            addLikedListUseCase = get(),
            removeLikedListUseCase = get(),
        )
    }
}
