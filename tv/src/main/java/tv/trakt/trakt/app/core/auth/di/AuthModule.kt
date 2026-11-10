package tv.trakt.trakt.app.core.auth.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.auth.AuthViewModel
import tv.trakt.trakt.app.core.auth.usecases.GetDeviceTokenUseCase
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.common.core.user.usecases.LoadUserProfileUseCase

internal val authModule = module {
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
            authRemoteSource = get(),
            getDeviceTokenUseCase = get(),
            loadUserProfileUseCase = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }
}
