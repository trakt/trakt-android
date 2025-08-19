package tv.trakt.trakt.app.core.lists.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.lists.ListsViewModel
import tv.trakt.trakt.app.core.lists.details.movies.MoviesWatchlistViewModel
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListViewModel
import tv.trakt.trakt.app.core.lists.details.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.app.core.lists.details.shows.ShowsWatchlistViewModel
import tv.trakt.trakt.app.core.lists.usecases.GetListsMoviesWatchlistUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsPersonalUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsShowsWatchlistUseCase

internal val listsModule = module {
    factory {
        GetListsShowsWatchlistUseCase(
            remoteSyncSource = get(),
            localShowSource = get(),
        )
    }

    factory {
        GetListsMoviesWatchlistUseCase(
            remoteSyncSource = get(),
            localMovieSource = get(),
        )
    }

    factory {
        GetListsPersonalUseCase(
            remoteProfileSource = get(),
        )
    }

    factory {
        GetPersonalListItemsUseCase(
            remoteSource = get(),
            localShowSource = get(),
            localMovieSource = get(),
        )
    }

    viewModel {
        ListsViewModel(
            getShowsWatchlistUseCase = get(),
            getMoviesWatchlistUseCase = get(),
            getPersonalUseCase = get(),
            showsLocalSyncSource = get(),
            moviesLocalSyncSource = get(),
        )
    }

    viewModel {
        MoviesWatchlistViewModel(
            getListItemsUseCase = get(),
        )
    }

    viewModel {
        ShowsWatchlistViewModel(
            getListItemsUseCase = get(),
        )
    }

    viewModel {
        PersonalListViewModel(
            savedStateHandle = get(),
            getListItemsUseCase = get(),
        )
    }
}
