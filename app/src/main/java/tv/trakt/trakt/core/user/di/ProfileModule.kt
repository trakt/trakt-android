package tv.trakt.trakt.core.user.di

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
import tv.trakt.trakt.core.auth.di.AUTH_PREFERENCES
import tv.trakt.trakt.core.user.data.local.UserListsLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserListsStorage
import tv.trakt.trakt.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserProgressStorage
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.UserWatchlistStorage
import tv.trakt.trakt.core.user.data.local.favorites.UserFavoritesLocalDataSource
import tv.trakt.trakt.core.user.data.local.favorites.UserFavoritesStorage
import tv.trakt.trakt.core.user.data.local.reactions.UserReactionsLocalDataSource
import tv.trakt.trakt.core.user.data.local.reactions.UserReactionsStorage
import tv.trakt.trakt.core.user.data.remote.UserApiClient
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.core.user.features.profile.ProfileViewModel
import tv.trakt.trakt.core.user.features.profile.sections.favorites.ProfileFavoritesViewModel
import tv.trakt.trakt.core.user.features.profile.sections.favorites.filters.GetFavoritesFilterUseCase
import tv.trakt.trakt.core.user.features.profile.sections.social.ProfileSocialViewModel
import tv.trakt.trakt.core.user.features.profile.sections.social.usecases.GetSocialFilterUseCase
import tv.trakt.trakt.core.user.usecase.GetUserProfileUseCase
import tv.trakt.trakt.core.user.usecase.LogoutUserUseCase
import tv.trakt.trakt.core.user.usecase.lists.LoadUserFavoritesUseCase
import tv.trakt.trakt.core.user.usecase.lists.LoadUserListsUseCase
import tv.trakt.trakt.core.user.usecase.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecase.progress.LoadUserProgressUseCase
import tv.trakt.trakt.core.user.usecase.reactions.LoadUserReactionsUseCase
import tv.trakt.trakt.core.user.usecase.social.LoadUserSocialUseCase

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
            localUserReactions = get(),
        )
    }

    viewModel {
        ProfileViewModel(
            sessionManager = get(),
            authorizeUseCase = get(),
            authorizePreferences = get(named(AUTH_PREFERENCES)),
            getProfileUseCase = get(),
            logoutUseCase = get(),
        )
    }

    viewModel {
        ProfileFavoritesViewModel(
            sessionManager = get(),
            loadFavoritesUseCase = get(),
            getFilterUseCase = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
        )
    }

    viewModel {
        ProfileSocialViewModel(
            sessionManager = get(),
            loadSocialUseCase = get(),
            getFilterUseCase = get(),
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
