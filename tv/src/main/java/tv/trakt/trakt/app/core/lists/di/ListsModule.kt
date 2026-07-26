package tv.trakt.trakt.app.core.lists.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.lists.ListsViewModel
import tv.trakt.trakt.app.core.lists.details.movies.MoviesWatchlistViewModel
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListViewModel
import tv.trakt.trakt.app.core.lists.details.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.app.core.lists.details.shows.ShowsWatchlistViewModel
import tv.trakt.trakt.app.core.lists.usecases.GetListsLikedUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsMoviesWatchlistUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsPersonalUseCase
import tv.trakt.trakt.app.core.lists.usecases.GetListsShowsWatchlistUseCase
import tv.trakt.trakt.app.core.lists.usecases.liked.AddLikedListUseCase
import tv.trakt.trakt.app.core.lists.usecases.liked.RemoveLikedListUseCase
import tv.trakt.trakt.common.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.common.core.user.data.local.watchlist.WatchlistUpdatesStorage

internal val listsModule = module {
    singleOf(::WatchlistUpdatesStorage) { bind<WatchlistUpdates>() }

    factory {
        GetListsShowsWatchlistUseCase(
            remoteSource = get(),
            localShowSource = get(),
        )
    }

    factory {
        GetListsMoviesWatchlistUseCase(
            remoteSource = get(),
            localMovieSource = get(),
        )
    }

    factory {
        GetListsPersonalUseCase(
            remoteProfileSource = get(),
        )
    }

    factory {
        GetListsLikedUseCase(
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

    factory {
        AddLikedListUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        RemoveLikedListUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    viewModel {
        ListsViewModel(
            getShowsWatchlistUseCase = get(),
            getMoviesWatchlistUseCase = get(),
            getPersonalUseCase = get(),
            getLikedUseCase = get(),
            showsLocalSyncSource = get(),
            moviesLocalSyncSource = get(),
            appLifecycleProvider = get(),
            cacheMarkerProvider = get(),
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
            collectionStateProvider = get(),
        )
    }
}
