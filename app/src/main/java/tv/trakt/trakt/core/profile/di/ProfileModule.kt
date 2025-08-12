package tv.trakt.trakt.core.profile.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.auth.di.AUTH_PREFERENCES
import tv.trakt.trakt.core.profile.ProfileViewModel
import tv.trakt.trakt.core.profile.usecase.GetUserProfileUseCase
import tv.trakt.trakt.core.profile.usecase.LogoutUserUseCase

internal val profileModule = module {
    factory {
        GetUserProfileUseCase(
            sessionManager = get(),
        )
    }

    factory {
        LogoutUserUseCase(
            sessionManager = get(),
        )
    }

    viewModel {
        ProfileViewModel(
            authorizeUseCase = get(),
            authorizePreferences = get(named(AUTH_PREFERENCES)),
            getProfileUseCase = get(),
            logoutUseCase = get(),
        )
    }
}
