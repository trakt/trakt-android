package tv.trakt.trakt.app.core.auth.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.auth.AuthViewModel
import tv.trakt.trakt.app.core.auth.usecases.GetDeviceCodeUseCase
import tv.trakt.trakt.app.core.auth.usecases.GetDeviceTokenUseCase
import tv.trakt.trakt.app.core.auth.usecases.LoadUserProfileUseCase
import tv.trakt.trakt.common.auth.TokenProvider

internal val authDataModule = module {
}

internal val authModule = module {
    factory {
        GetDeviceCodeUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetDeviceTokenUseCase(
            remoteSource = get(),
            tokenProvider = get<TokenProvider>(),
        )
    }

    factory {
        LoadUserProfileUseCase(
            remoteSource = get(),
            sessionManager = get(),
        )
    }

    viewModel {
        AuthViewModel(
            getDeviceCodeUseCase = get(),
            getDeviceTokenUseCase = get(),
            loadUserProfileUseCase = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }
}
