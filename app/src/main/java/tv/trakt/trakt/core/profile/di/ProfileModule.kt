package tv.trakt.trakt.core.profile.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.profile.ProfileViewModel
import tv.trakt.trakt.core.profile.sections.favorites.ProfileFavoritesViewModel
import tv.trakt.trakt.core.profile.sections.favorites.all.AllFavoritesViewModel
import tv.trakt.trakt.core.profile.sections.favorites.filters.GetFavoritesFilterUseCase
import tv.trakt.trakt.core.profile.sections.history.ProfileHistoryViewModel
import tv.trakt.trakt.core.profile.sections.social.ProfileSocialViewModel
import tv.trakt.trakt.core.profile.sections.social.usecases.GetSocialFilterUseCase
import tv.trakt.trakt.core.profile.sections.thismonth.usecases.GetThisMonthUseCase
import tv.trakt.trakt.core.user.data.local.UserListsLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserListsStorage
import tv.trakt.trakt.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserProgressStorage
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserWatchlistStorage
import tv.trakt.trakt.core.user.data.local.favorites.UserFavoritesLocalDataSource
import tv.trakt.trakt.core.user.data.local.favorites.UserFavoritesStorage
import tv.trakt.trakt.core.user.data.local.ratings.UserRatingsLocalDataSource
import tv.trakt.trakt.core.user.data.local.ratings.UserRatingsStorage
import tv.trakt.trakt.core.user.data.local.reactions.UserReactionsLocalDataSource
import tv.trakt.trakt.core.user.data.local.reactions.UserReactionsStorage
import tv.trakt.trakt.core.user.data.remote.UserApiClient
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.core.user.usecases.GetUserProfileUseCase
import tv.trakt.trakt.core.user.usecases.LogoutUserUseCase
import tv.trakt.trakt.core.user.usecases.lists.LoadUserFavoritesUseCase
import tv.trakt.trakt.core.user.usecases.lists.LoadUserListsUseCase
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import tv.trakt.trakt.core.user.usecases.reactions.LoadUserReactionsUseCase
import tv.trakt.trakt.core.user.usecases.social.LoadUserSocialUseCase

internal const val PROFILE_PREFERENCES = "profile_preferences_mobile"

internal val profileDataModule = module {
    single<UserRemoteDataSource> {
        UserApiClient(
            usersApi = get(),
            historyApi = get(),
            calendarsApi = get(),
        )
    }

    single<UserWatchlistLocalDataSource> {
        UserWatchlistStorage()
    }

    single<UserProgressLocalDataSource> {
        UserProgressStorage()
    }

    single<UserListsLocalDataSource> {
        UserListsStorage()
    }

    single<UserReactionsLocalDataSource> {
        UserReactionsStorage()
    }

    single<UserFavoritesLocalDataSource> {
        UserFavoritesStorage()
    }

    single<UserRatingsLocalDataSource> {
        UserRatingsStorage()
    }

    single<DataStore<Preferences>>(named(PROFILE_PREFERENCES)) {
        createStore(
            context = androidApplication(),
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
        GetFavoritesFilterUseCase(
            dataStore = get(named(PROFILE_PREFERENCES)),
        )
    }

    factory {
        GetSocialFilterUseCase(
            dataStore = get(named(PROFILE_PREFERENCES)),
        )
    }

    factory {
        GetThisMonthUseCase(
            loadUserProgressUseCase = get(),
        )
    }

    factory {
        LoadUserWatchlistUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        LoadUserSocialUseCase(
            remoteSource = get(),
        )
    }

    factory {
        LoadUserFavoritesUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        LoadUserListsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        LoadUserProgressUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        LoadUserReactionsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }
    factory {
        LoadUserRatingsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        LogoutUserUseCase(
            appContext = androidApplication(),
            sessionManager = get(),
            apiClients = get(named("apiClients")),
            localUpNext = get(),
            localUpcoming = get(),
            localSocial = get(),
            localPersonal = get(),
            localRecentSearch = get(),
            localListsPersonal = get(),
            localListsItemsPersonal = get(),
            localRecommendedShows = get(),
            localRecommendedMovies = get(),
            localUserWatchlist = get(),
            localUserProgress = get(),
            localUserLists = get(),
            localUserFavorites = get(),
            localUserReactions = get(),
            localUserRatings = get(),
        )
    }

    viewModel {
        ProfileViewModel(
            sessionManager = get(),
            getThisMonthUseCase = get(),
            logoutUseCase = get(),
            analytics = get(),
        )
    }

    viewModel {
        ProfileHistoryViewModel(
            getPersonalActivityUseCase = get(),
            allActivitySource = get(),
            showLocalDataSource = get(),
            showUpdatesSource = get(),
            episodeUpdatesSource = get(),
            episodeLocalDataSource = get(),
            movieUpdates = get(),
            movieLocalDataSource = get(),
            sessionManager = get(),
        )
    }

    viewModel {
        ProfileFavoritesViewModel(
            sessionManager = get(),
            loadFavoritesUseCase = get(),
            getFilterUseCase = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            favoritesUpdates = get(),
        )
    }

    viewModel {
        ProfileSocialViewModel(
            sessionManager = get(),
            loadSocialUseCase = get(),
            getFilterUseCase = get(),
        )
    }

    viewModel {
        AllFavoritesViewModel(
            sessionManager = get(),
            loadFavoritesUseCase = get(),
            getFilterUseCase = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            favoritesUpdates = get(),
            analytics = get(),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, PROFILE_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(PROFILE_PREFERENCES) },
    )
}
