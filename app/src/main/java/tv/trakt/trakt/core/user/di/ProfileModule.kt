package tv.trakt.trakt.core.user.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.auth.di.AUTH_PREFERENCES
import tv.trakt.trakt.core.user.ProfileViewModel
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserWatchlistStorage
import tv.trakt.trakt.core.user.data.remote.UserApiClient
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.core.user.usecase.GetUserProfileUseCase
import tv.trakt.trakt.core.user.usecase.LogoutUserUseCase
import tv.trakt.trakt.core.user.usecase.watchlist.LoadUserWatchlistUseCase

internal val profileDataModule = module {
    single<UserRemoteDataSource> {
        UserApiClient(
            usersApi = get(),
            historyApi = get(),
            calendarsApi = get(),
        )
    }

    single<UserWatchlistLocalDataSource> {
        UserWatchlistStorage()
    }
}

internal val profileModule = module {
    factory {
        GetUserProfileUseCase(
            sessionManager = get(),
            remoteSource = get(),
        )
    }

    factory {
        LoadUserWatchlistUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        LogoutUserUseCase(
            sessionManager = get(),
            apiClients = get(named("apiClients")),
            localUpNext = get(),
            localUpcoming = get(),
            localSocial = get(),
            localPersonal = get(),
            localRecentSearch = get(),
            localListsPersonal = get(),
            localListsItemsPersonal = get(),
            localRecommendedShows = get(),
            localRecommendedMovies = get(),
            localUserWatchlist = get(),
        )
    }

    viewModel {
        ProfileViewModel(
            sessionManager = get(),
            authorizeUseCase = get(),
            authorizePreferences = get(named(AUTH_PREFERENCES)),
            getProfileUseCase = get(),
            logoutUseCase = get(),
        )
    }
}
