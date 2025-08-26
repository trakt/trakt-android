package tv.trakt.trakt.core.home.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.home.HomeViewModel
import tv.trakt.trakt.core.home.sections.upnext.HomeUpNextViewModel
import tv.trakt.trakt.core.home.sections.upnext.usecases.GetUpNextUseCase

internal val homeModule = module {

    factory {
        GetUpNextUseCase(
            remoteSyncSource = get(),
        )
    }

    viewModel {
        HomeViewModel(
            sessionManager = get(),
        )
    }

    viewModel {
        HomeUpNextViewModel(
            getUpNextUseCase = get(),
        )
    }
}
