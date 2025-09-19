package tv.trakt.trakt.core.main.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.main.MainViewModel

internal val mainModule = module {
    viewModel {
        MainViewModel(
            sessionManager = get(),
            loadUserProgressUseCase = get(),
        )
    }
}
