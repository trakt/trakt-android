package tv.trakt.trakt.app.core.profile.di

import androidx.lifecycle.SavedStateHandle
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.app.core.profile.ProfileViewModel
import tv.trakt.trakt.app.core.profile.sections.favorites.ProfileFavoritesViewModel
import tv.trakt.trakt.app.core.profile.sections.favorites.usecases.GetFavoriteMoviesUseCase
import tv.trakt.trakt.app.core.profile.sections.favorites.usecases.GetFavoriteShowsUseCase
import tv.trakt.trakt.app.core.profile.sections.favorites.viewall.ProfileFavoritesViewAllViewModel
import tv.trakt.trakt.app.core.profile.sections.history.ProfileHistoryViewModel
import tv.trakt.trakt.app.core.profile.sections.history.usecases.GetProfileHistoryUseCase
import tv.trakt.trakt.app.core.profile.sections.history.usecases.SyncProfileHistoryUseCase
import tv.trakt.trakt.app.core.profile.sections.history.viewall.ProfileHistoryViewAllViewModel
import tv.trakt.trakt.app.core.profile.sections.library.ProfileLibraryViewModel
import tv.trakt.trakt.app.core.profile.sections.library.usecases.GetProfileLibraryUseCase
import tv.trakt.trakt.app.core.profile.sections.library.viewall.ProfileLibraryViewAllViewModel
import tv.trakt.trakt.app.core.profile.usecases.LogoutProfileUseCase
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserLikedListsUseCase
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase

internal val profileModule = module {
    factory {
        LogoutProfileUseCase(
            apiClients = get(named("apiClients")),
            sessionManager = get(),
            analytics = get(),
            showsSyncLocalSource = get(),
            moviesSyncLocalSource = get(),
            episodesSyncLocalSource = get(),
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
        GetProfileLibraryUseCase(
            remoteSource = get(),
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

    factoryOf(::LoadUserWatchlistUseCase)
    factoryOf(::LoadUserProgressUseCase)

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
            dataChanges = get(),
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
        ProfileFavoritesViewModel(
            getFavoriteShowsCase = get(),
            getFavoriteMoviesCase = get(),
            appLifecycleProvider = get(),
        )
    }

    viewModel {
        ProfileFavoritesViewAllViewModel(
            getFavoriteShowsCase = get(),
            getFavoriteMoviesCase = get(),
        )
    }

    viewModel {
        ProfileLibraryViewModel(
            getLibraryUseCase = get(),
            appLifecycleProvider = get(),
        )
    }

    viewModel {
        ProfileLibraryViewAllViewModel(
            getLibraryUseCase = get(),
        )
    }
}
