package tv.trakt.app.tv.core.lists.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.app.tv.core.lists.ListsViewModel
import tv.trakt.app.tv.core.lists.details.movies.MoviesWatchlistViewModel
import tv.trakt.app.tv.core.lists.details.shows.ShowsWatchlistViewModel
import tv.trakt.app.tv.core.lists.usecases.GetListsMoviesWatchlistUseCase
import tv.trakt.app.tv.core.lists.usecases.GetListsShowsWatchlistUseCase

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

    viewModel {
        ListsViewModel(
            getShowsWatchlistUseCase = get(),
            getMoviesWatchlistUseCase = get(),
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
}
