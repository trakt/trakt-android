package tv.trakt.trakt.core.profile.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.auth.di.AUTH_PREFERENCES
import tv.trakt.trakt.core.lists.di.LISTS_MOVIES_WATCHLIST_STORAGE
import tv.trakt.trakt.core.lists.di.LISTS_SHOWS_WATCHLIST_STORAGE
import tv.trakt.trakt.core.lists.di.LISTS_WATCHLIST_STORAGE
import tv.trakt.trakt.core.profile.ProfileViewModel
import tv.trakt.trakt.core.profile.data.remote.UserApiClient
import tv.trakt.trakt.core.profile.data.remote.UserRemoteDataSource
import tv.trakt.trakt.core.profile.usecase.GetUserProfileUseCase
import tv.trakt.trakt.core.profile.usecase.LogoutUserUseCase

internal val profileDataModule = module {
    single<UserRemoteDataSource> {
        UserApiClient(
            usersApi = get(),
            historyApi = get(),
            calendarsApi = get(),
        )
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
        LogoutUserUseCase(
            sessionManager = get(),
            apiClients = get(named("apiClients")),
            localUpNext = get(),
            localWatchlist = get(),
            localUpcoming = get(),
            localSocial = get(),
            localPersonal = get(),
            localRecentSearch = get(),
            localListsPersonal = get(),
            localListsItemsPersonal = get(),
            localListsWatchlist = get(named(LISTS_WATCHLIST_STORAGE)),
            localListsShowsWatchlist = get(named(LISTS_SHOWS_WATCHLIST_STORAGE)),
            localListsMoviesWatchlist = get(named(LISTS_MOVIES_WATCHLIST_STORAGE)),
            localRecommendedShows = get(),
            localRecommendedMovies = get(),
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
