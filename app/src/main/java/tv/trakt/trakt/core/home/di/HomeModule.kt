package tv.trakt.trakt.core.home.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.home.HomeViewModel

internal val homeModule = module {
    viewModel {
        HomeViewModel(
            sessionManager = get(),
        )
    }
}
