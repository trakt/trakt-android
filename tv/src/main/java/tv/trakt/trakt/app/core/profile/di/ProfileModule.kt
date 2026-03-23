package tv.trakt.trakt.app.core.profile.di

import androidx.lifecycle.SavedStateHandle
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.app.core.profile.ProfileViewModel
import tv.trakt.trakt.app.core.profile.data.remote.ProfileApiClient
import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.app.core.profile.sections.favorites.movies.ProfileFavoriteMoviesViewModel
import tv.trakt.trakt.app.core.profile.sections.favorites.movies.usecases.GetFavoriteMoviesUseCase
import tv.trakt.trakt.app.core.profile.sections.favorites.movies.viewall.ProfileFavoriteMoviesViewAllViewModel
import tv.trakt.trakt.app.core.profile.sections.favorites.shows.ProfileFavoriteShowsViewModel
import tv.trakt.trakt.app.core.profile.sections.favorites.shows.usecases.GetFavoriteShowsUseCase
import tv.trakt.trakt.app.core.profile.sections.favorites.shows.viewall.ProfileFavoriteShowsViewAllViewModel
import tv.trakt.trakt.app.core.profile.sections.history.ProfileHistoryViewModel
import tv.trakt.trakt.app.core.profile.sections.history.usecases.GetProfileHistoryUseCase
import tv.trakt.trakt.app.core.profile.sections.history.usecases.SyncProfileHistoryUseCase
import tv.trakt.trakt.app.core.profile.sections.history.viewall.ProfileHistoryViewAllViewModel
import tv.trakt.trakt.app.core.profile.usecases.LogoutProfileUseCase
import tv.trakt.trakt.common.core.user.data.local.liked.UserLikedListsLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.liked.UserLikedListsStorage
import tv.trakt.trakt.common.core.user.data.remote.UserApiClient
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserLikedListsUseCase

internal val profileDataModule = module {
    single<ProfileRemoteDataSource> {
        ProfileApiClient(
            usersApi = get(),
            calendarsApi = get(),
            historyApi = get(),
        )
    }

    single<UserRemoteDataSource> {
        UserApiClient(
            usersApi = get(),
            historyApi = get(),
            calendarsApi = get(),
            syncApi = get(),
            cacheMarkerProvider = get(),
        )
    }

    single<UserLikedListsLocalDataSource> {
        UserLikedListsStorage()
    }
}

internal val profileModule = module {

    factory {
        LogoutProfileUseCase(
            apiClients = get(named("apiClients")),
            sessionManager = get(),
            showsSyncLocalDataSource = get(),
            moviesSyncLocalDataSource = get(),
            episodesSyncLocalDataSource = get(),
            recentSearchLocalDataSource = get(),
        )
    }

    factory {
        GetProfileHistoryUseCase(
            remoteUserSource = get(),
            localMoviesSource = get(),
            localEpisodesSource = get(),
        )
    }

    factory {
        SyncProfileHistoryUseCase(
            localShowsSyncSource = get(),
            localMoviesSyncSource = get(),
            localEpisodesSyncSource = get(),
        )
    }

    factory {
        GetFavoriteShowsUseCase(
            remoteUserSource = get(),
            localShowsSource = get(),
        )
    }

    factory {
        GetFavoriteMoviesUseCase(
            remoteUserSource = get(),
            localMoviesSource = get(),
        )
    }

    factory {
        LoadUserLikedListsUseCase(
            sessionManager = get(),
            remoteSource = get(),
            localSource = get(),
        )
    }

    viewModel { (_: SavedStateHandle) ->
        ProfileViewModel(
            sessionManager = get(),
            logoutUseCase = get(),
        )
    }

    viewModel {
        ProfileHistoryViewModel(
            getHistoryCase = get(),
            syncHistoryCase = get(),
            appLifecycleProvider = get(),
        )
    }

    viewModel {
        ProfileHistoryViewAllViewModel(
            getHistoryCase = get(),
            syncHistoryCase = get(),
        )
    }

    viewModel {
        ProfileFavoriteShowsViewModel(
            getFavoriteShowsCase = get(),
            appLifecycleProvider = get(),
        )
    }

    viewModel {
        ProfileFavoriteShowsViewAllViewModel(
            getFavoriteShowsCase = get(),
        )
    }

    viewModel {
        ProfileFavoriteMoviesViewModel(
            getFavoriteMoviesCase = get(),
            appLifecycleProvider = get(),
        )
    }

    viewModel {
        ProfileFavoriteMoviesViewAllViewModel(
            getFavoriteMoviesCase = get(),
        )
    }
}
