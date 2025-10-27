package tv.trakt.trakt.core.main.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.auth.di.AUTH_PREFERENCES
import tv.trakt.trakt.core.main.MainViewModel

internal val mainModule = module {
    viewModel {
        MainViewModel(
            sessionManager = get(),
            loadUserProgressUseCase = get(),
            loadUserWatchlistUseCase = get(),
            authorizePreferences = get(named(AUTH_PREFERENCES)),
            authorizeUseCase = get(),
            getUserUseCase = get(),
            logoutUserUseCase = get(),
        )
    }
}
