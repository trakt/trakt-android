package tv.trakt.trakt.core.home.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.home.HomeViewModel
import tv.trakt.trakt.core.home.sections.activity.HomeSocialViewModel
import tv.trakt.trakt.core.home.sections.activity.usecases.GetSocialActivityUseCase
import tv.trakt.trakt.core.home.sections.upnext.HomeUpNextViewModel
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextStorage
import tv.trakt.trakt.core.home.sections.upnext.usecases.GetUpNextUseCase
import tv.trakt.trakt.core.home.sections.watchlist.HomeWatchlistViewModel
import tv.trakt.trakt.core.home.sections.watchlist.data.local.HomeWatchlistLocalDataSource
import tv.trakt.trakt.core.home.sections.watchlist.data.local.HomeWatchlistStorage
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetWatchlistMoviesUseCase

internal val homeDataModule = module {

    single<HomeUpNextLocalDataSource> {
        HomeUpNextStorage()
    }

    single<HomeWatchlistLocalDataSource> {
        HomeWatchlistStorage()
    }
}

internal val homeModule = module {

    factory {
        GetUpNextUseCase(
            remoteSyncSource = get(),
            localDataSource = get(),
        )
    }

    factory {
        GetWatchlistMoviesUseCase(
            remoteSyncSource = get(),
            localDataSource = get(),
        )
    }

    factory {
        GetSocialActivityUseCase(
            remoteSource = get(),
        )
    }

    viewModel {
        HomeViewModel(
            sessionManager = get(),
        )
    }

    viewModel {
        HomeUpNextViewModel(
            getUpNextUseCase = get(),
        )
    }

    viewModel {
        HomeWatchlistViewModel(
            getWatchlistUseCase = get(),
        )
    }

    viewModel {
        HomeSocialViewModel(
            getSocialActivityUseCase = get(),
        )
    }
}
