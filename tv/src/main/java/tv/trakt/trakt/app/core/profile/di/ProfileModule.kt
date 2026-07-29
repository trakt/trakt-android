package tv.trakt.trakt.app.core.profile.di

import androidx.lifecycle.SavedStateHandle
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.app.core.profile.ProfileViewModel
import tv.trakt.trakt.app.core.profile.data.remote.ProfileApiClient
import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
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
import tv.trakt.trakt.common.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.UserProgressStorage
import tv.trakt.trakt.common.core.user.data.local.liked.UserLikedListsLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.liked.UserLikedListsStorage
import tv.trakt.trakt.common.core.user.data.local.watchlist.minimal.UserWatchlistMinimalLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.watchlist.minimal.UserWatchlistMinimalStorage
import tv.trakt.trakt.common.core.user.data.remote.UserApiClient
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.calendar.UserCalendarApiClient
import tv.trakt.trakt.common.core.user.data.remote.calendar.UserCalendarRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.favorites.UserFavoritesApiClient
import tv.trakt.trakt.common.core.user.data.remote.favorites.UserFavoritesRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryApiClient
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.otherlists.UserOtherListsApiClient
import tv.trakt.trakt.common.core.user.data.remote.otherlists.UserOtherListsRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.personallists.UserPersonalListsApiClient
import tv.trakt.trakt.common.core.user.data.remote.personallists.UserPersonalListsRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.ratings.UserRatingsApiClient
import tv.trakt.trakt.common.core.user.data.remote.ratings.UserRatingsRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialApiClient
import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistApiClient
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistRemoteDataSource
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserLikedListsUseCase
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.core.user.usecases.progress.updates.ProgressUpdates
import tv.trakt.trakt.common.core.user.usecases.progress.updates.ProgressUpdatesStorage

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
            syncApi = get(),
            cacheMarkerProvider = get(),
        )
    }

    single<UserHistoryRemoteDataSource> {
        UserHistoryApiClient(
            historyApi = get(),
        )
    }

    single<UserWatchlistRemoteDataSource> {
        UserWatchlistApiClient(
            usersApi = get(),
            v3Api = get(),
        )
    }

    single<UserFavoritesRemoteDataSource> {
        UserFavoritesApiClient(
            usersApi = get(),
        )
    }

    single<UserOtherListsRemoteDataSource> {
        UserOtherListsApiClient(
            usersApi = get(),
        )
    }

    single<UserCalendarRemoteDataSource> {
        UserCalendarApiClient(
            calendarsApi = get(),
        )
    }

    single<UserSocialRemoteDataSource> {
        UserSocialApiClient(
            usersApi = get(),
            cacheMarker = get(),
        )
    }

    single<UserRatingsRemoteDataSource> {
        UserRatingsApiClient(
            usersApi = get(),
        )
    }

    single<UserPersonalListsRemoteDataSource> {
        UserPersonalListsApiClient(
            usersApi = get(),
            v3Api = get(),
        )
    }

    single<UserLikedListsLocalDataSource> {
        UserLikedListsStorage()
    }

    singleOf(::UserWatchlistMinimalStorage) { bind<UserWatchlistMinimalLocalDataSource>() }
    singleOf(::ProgressUpdatesStorage) { bind<ProgressUpdates>() }
    singleOf(::UserProgressStorage) { bind<UserProgressLocalDataSource>() }
}

internal val profileModule = module {

    factory {
        LogoutProfileUseCase(
            apiClients = get(named("apiClients")),
            sessionManager = get(),
            analytics = get(),
            showsSyncLocalDataSource = get(),
            moviesSyncLocalDataSource = get(),
            episodesSyncLocalDataSource = get(),
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
            scrobbleUpdates = get(),
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
