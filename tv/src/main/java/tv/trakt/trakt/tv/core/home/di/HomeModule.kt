package tv.trakt.trakt.tv.core.home.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.tv.core.home.HomeViewModel
import tv.trakt.trakt.tv.core.home.sections.movies.availablenow.HomeAvailableNowViewModel
import tv.trakt.trakt.tv.core.home.sections.movies.availablenow.usecases.GetAvailableNowMoviesUseCase
import tv.trakt.trakt.tv.core.home.sections.movies.comingsoon.HomeComingSoonViewModel
import tv.trakt.trakt.tv.core.home.sections.movies.comingsoon.usecases.GetComingSoonMoviesUseCase
import tv.trakt.trakt.tv.core.home.sections.shows.upcoming.HomeUpcomingViewModel
import tv.trakt.trakt.tv.core.home.sections.shows.upcoming.usecases.GetUpcomingUseCase
import tv.trakt.trakt.tv.core.home.sections.shows.upnext.HomeUpNextViewModel
import tv.trakt.trakt.tv.core.home.sections.shows.upnext.usecases.GetUpNextUseCase

internal val homeModule = module {
    factory {
        GetAvailableNowMoviesUseCase(
            remoteSyncSource = get(),
            localMovieSource = get(),
        )
    }

    factory {
        GetComingSoonMoviesUseCase(
            remoteSyncSource = get(),
            localMovieSource = get(),
        )
    }

    factory {
        GetUpcomingUseCase(
            remoteUserSource = get(),
            localShowSource = get(),
            localEpisodeSource = get(),
        )
    }

    factory {
        GetUpNextUseCase(
            remoteShowsSource = get(),
            localShowSource = get(),
            localEpisodeSource = get(),
        )
    }

    viewModel {
        HomeViewModel(
            sessionManager = get(),
        )
    }

    viewModel {
        HomeAvailableNowViewModel(
            getAvailableNowUseCase = get(),
            localSyncSource = get(),
        )
    }

    viewModel {
        HomeComingSoonViewModel(
            getComingSoonUseCase = get(),
            localSyncSource = get(),
        )
    }

    viewModel {
        HomeUpcomingViewModel(
            getUpcomingUseCase = get(),
            localShowsSyncSource = get(),
            localEpisodesSyncSource = get(),
        )
    }

    viewModel {
        HomeUpNextViewModel(
            getUpNextUseCase = get(),
            localShowsSyncSource = get(),
            localEpisodesSyncSource = get(),
        )
    }
}
