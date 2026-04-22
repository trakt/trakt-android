package tv.trakt.trakt.app.core.home.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.home.HomeViewModel
import tv.trakt.trakt.app.core.home.sections.history.HomeHistoryViewModel
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.HomeUpcomingViewModel
import tv.trakt.trakt.app.core.home.sections.shows.upcoming.usecases.GetUpcomingUseCase
import tv.trakt.trakt.app.core.home.sections.shows.upnext.HomeUpNextViewModel
import tv.trakt.trakt.app.core.home.sections.shows.upnext.usecases.GetMoviesUpNextUseCase
import tv.trakt.trakt.app.core.home.sections.shows.upnext.usecases.GetShowsUpNextUseCase
import tv.trakt.trakt.app.core.home.sections.shows.upnext.viewall.UpNextViewAllViewModel
import tv.trakt.trakt.app.core.home.sections.social.HomeSocialViewModel
import tv.trakt.trakt.app.core.home.sections.social.usecases.GetSocialActivityUseCase
import tv.trakt.trakt.app.core.home.sections.social.viewall.SocialViewAllViewModel
import tv.trakt.trakt.app.core.home.sections.startwatching.HomeWatchlistViewModel
import tv.trakt.trakt.app.core.home.sections.startwatching.usecases.GetHomeMoviesWatchlistItemsUseCase
import tv.trakt.trakt.app.core.home.sections.startwatching.usecases.GetHomeShowsWatchlistItemsUseCase
import tv.trakt.trakt.app.core.home.sections.startwatching.viewall.WatchlistViewAllViewModel

internal val homeModule = module {
    factory {
        GetHomeMoviesWatchlistItemsUseCase(
            remoteSyncSource = get(),
            localMovieSource = get(),
        )
    }

    factory {
        GetHomeShowsWatchlistItemsUseCase(
            remoteSyncSource = get(),
            localShowsSource = get(),
        )
    }

    factory {
        GetUpcomingUseCase(
            remoteUserSource = get(),
            localShowSource = get(),
            localMovieSource = get(),
            localEpisodeSource = get(),
        )
    }

    factory {
        GetShowsUpNextUseCase(
            remoteShowsSource = get(),
            localShowSource = get(),
            localEpisodeSource = get(),
        )
    }

    factory {
        GetMoviesUpNextUseCase(
            remoteMoviesSource = get(),
            localMovieSource = get(),
        )
    }

    factory {
        GetSocialActivityUseCase(
            remoteProfileSource = get(),
        )
    }

    viewModel {
        HomeViewModel(
            sessionManager = get(),
        )
    }

    viewModel {
        HomeWatchlistViewModel(
            getMoviesUseCase = get(),
            getShowsUseCase = get(),
            localMoviesSyncSource = get(),
            localShowsSyncSource = get(),
            scrobbleUpdates = get(),
            appLifecycleProvider = get(),
        )
    }

    viewModel {
        HomeUpcomingViewModel(
            getUpcomingUseCase = get(),
            localShowsSyncSource = get(),
            localMoviesSyncSource = get(),
            localEpisodesSyncSource = get(),
            appLifecycleProvider = get(),
        )
    }

    viewModel {
        HomeUpNextViewModel(
            getShowsUpNextUseCase = get(),
            getMoviesUpNextUseCase = get(),
            localShowsSyncSource = get(),
            localEpisodesSyncSource = get(),
            scrobbleUpdates = get(),
            appLifecycleProvider = get(),
        )
    }

    viewModel {
        HomeSocialViewModel(
            getSocialActivityUseCase = get(),
        )
    }

    viewModel {
        UpNextViewAllViewModel(
            getShowsUpNextUseCase = get(),
            getMoviesUpNextUseCase = get(),
            localShowsSyncSource = get(),
            localEpisodesSyncSource = get(),
        )
    }

    viewModel {
        WatchlistViewAllViewModel(
            getShowsUseCase = get(),
            getMoviesUseCase = get(),
            localMoviesDataSource = get(),
            localShowsDataSource = get(),
        )
    }

    viewModel {
        SocialViewAllViewModel(
            getSocialActivityUseCase = get(),
        )
    }

    viewModel {
        HomeHistoryViewModel(
            getHistoryCase = get(),
            syncHistoryCase = get(),
            scrobbleUpdates = get(),
            appLifecycleProvider = get(),
        )
    }
}
