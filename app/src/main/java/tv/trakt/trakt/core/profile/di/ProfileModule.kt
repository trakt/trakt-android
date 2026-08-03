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
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.core.home.watchlist.data.HomeWatchlistLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.UserListsLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.UserListsStorage
import tv.trakt.trakt.common.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.UserProgressStorage
import tv.trakt.trakt.common.core.user.data.local.favorites.UserFavoritesLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.favorites.UserFavoritesStorage
import tv.trakt.trakt.common.core.user.data.local.library.UserLibraryLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.library.UserLibraryStorage
import tv.trakt.trakt.common.core.user.data.local.liked.UserLikedListsLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.liked.UserLikedListsStorage
import tv.trakt.trakt.common.core.user.data.local.ratings.UserRatingsLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.ratings.UserRatingsStorage
import tv.trakt.trakt.common.core.user.data.local.reactions.UserReactionsLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.reactions.UserReactionsStorage
import tv.trakt.trakt.common.core.user.data.local.watchlist.UserWatchlistLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.watchlist.UserWatchlistStorage
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
import tv.trakt.trakt.common.core.user.data.remote.smartlists.UserSmartListsApiClient
import tv.trakt.trakt.common.core.user.data.remote.smartlists.UserSmartListsRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialApiClient
import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistApiClient
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistRemoteDataSource
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserFavoritesUseCase
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserLibraryUseCase
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserLikedListsUseCase
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserListsUseCase
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.core.user.usecases.progress.updates.ProgressUpdates
import tv.trakt.trakt.common.core.user.usecases.progress.updates.ProgressUpdatesStorage
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.profile.ProfileViewModel
import tv.trakt.trakt.core.profile.sections.activity.ProfileActivityViewModel
import tv.trakt.trakt.core.profile.sections.activity.all.ProfileAllActivityViewModel
import tv.trakt.trakt.core.profile.sections.activity.data.local.comments.ProfileCommentsLocalDataSource
import tv.trakt.trakt.core.profile.sections.activity.data.local.comments.ProfileCommentsStorage
import tv.trakt.trakt.core.profile.sections.activity.data.local.ratings.ProfileRatingsLocalDataSource
import tv.trakt.trakt.core.profile.sections.activity.data.local.ratings.ProfileRatingsStorage
import tv.trakt.trakt.core.profile.sections.activity.usecases.GetProfileCommentsUseCase
import tv.trakt.trakt.core.profile.sections.activity.usecases.GetProfileRatingsUseCase
import tv.trakt.trakt.core.profile.sections.activity.usecases.filters.GetActivityFilterUseCase
import tv.trakt.trakt.core.profile.sections.favorites.ProfileFavoritesViewModel
import tv.trakt.trakt.core.profile.sections.favorites.all.AllFavoritesViewModel
import tv.trakt.trakt.core.profile.sections.favorites.context.movie.FavoriteMovieContextViewModel
import tv.trakt.trakt.core.profile.sections.favorites.context.show.FavoriteShowContextViewModel
import tv.trakt.trakt.core.profile.sections.history.ProfileHistoryViewModel
import tv.trakt.trakt.core.profile.sections.library.ProfileLibraryViewModel
import tv.trakt.trakt.core.profile.sections.library.all.AllLibraryViewModel
import tv.trakt.trakt.core.profile.sections.progress.ProfileProgressViewModel
import tv.trakt.trakt.core.profile.sections.progress.all.AllProgressViewModel
import tv.trakt.trakt.core.profile.sections.progress.data.local.completed.ProgressCompletedLocalDataSource
import tv.trakt.trakt.core.profile.sections.progress.data.local.completed.ProgressCompletedStorage
import tv.trakt.trakt.core.profile.sections.progress.data.local.dropped.ProgressDroppedLocalDataSource
import tv.trakt.trakt.core.profile.sections.progress.data.local.dropped.ProgressDroppedStorage
import tv.trakt.trakt.core.profile.sections.progress.data.local.watching.ProgressWatchingLocalDataSource
import tv.trakt.trakt.core.profile.sections.progress.data.local.watching.ProgressWatchingStorage
import tv.trakt.trakt.core.profile.sections.progress.filters.GetProgressFilterUseCase
import tv.trakt.trakt.core.profile.sections.progress.usecase.GetProgressCompleteUseCase
import tv.trakt.trakt.core.profile.sections.progress.usecase.GetProgressDroppedUseCase
import tv.trakt.trakt.core.profile.sections.progress.usecase.GetProgressWatchingUseCase
import tv.trakt.trakt.core.profile.sections.screentime.ProfileScreenTimeViewModel
import tv.trakt.trakt.core.profile.sections.screentime.all.ScreenTimeAllViewModel
import tv.trakt.trakt.core.profile.sections.screentime.data.local.ProfileProfileScreenTimeStorage
import tv.trakt.trakt.core.profile.sections.screentime.data.local.ProfileScreenTimeLocalDataSource
import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData
import tv.trakt.trakt.core.profile.sections.screentime.usecase.GetScreenTimeUseCase
import tv.trakt.trakt.core.profile.sections.social.ProfileSocialViewModel
import tv.trakt.trakt.core.profile.sections.social.all.AllProfileSocialViewModel
import tv.trakt.trakt.core.profile.sections.social.usecases.GetSocialFilterUseCase
import tv.trakt.trakt.core.profile.sections.thismonth.usecases.GetProfileStatsUseCase
import tv.trakt.trakt.core.user.usecases.LoadUserProfileUseCase
import tv.trakt.trakt.core.user.usecases.LogoutUserUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import tv.trakt.trakt.core.user.usecases.reactions.LoadUserReactionsUseCase
import tv.trakt.trakt.core.user.usecases.social.LoadUserSocialRequestsUseCase
import tv.trakt.trakt.core.user.usecases.social.LoadUserSocialUseCase
import tv.trakt.trakt.core.user.usecases.social.updates.SocialUpdates
import tv.trakt.trakt.core.user.usecases.social.updates.SocialUpdatesStorage

internal const val PROFILE_PREFERENCES = "profile_preferences_mobile"

internal val profileDataModule = module {
    singleOf(::UserApiClient) { bind<UserRemoteDataSource>() }
    singleOf(::UserHistoryApiClient) { bind<UserHistoryRemoteDataSource>() }
    singleOf(::UserWatchlistApiClient) { bind<UserWatchlistRemoteDataSource>() }
    singleOf(::UserFavoritesApiClient) { bind<UserFavoritesRemoteDataSource>() }
    singleOf(::UserOtherListsApiClient) { bind<UserOtherListsRemoteDataSource>() }
    singleOf(::UserCalendarApiClient) { bind<UserCalendarRemoteDataSource>() }
    singleOf(::UserRatingsApiClient) { bind<UserRatingsRemoteDataSource>() }
    singleOf(::UserPersonalListsApiClient) { bind<UserPersonalListsRemoteDataSource>() }
    singleOf(::UserSmartListsApiClient) { bind<UserSmartListsRemoteDataSource>() }
    singleOf(::UserSocialApiClient) { bind<UserSocialRemoteDataSource>() }
    singleOf(::UserWatchlistMinimalStorage) { bind<UserWatchlistMinimalLocalDataSource>() }
    singleOf(::UserProgressStorage) { bind<UserProgressLocalDataSource>() }
    singleOf(::UserListsStorage) { bind<UserListsLocalDataSource>() }
    singleOf(::UserLikedListsStorage) { bind<UserLikedListsLocalDataSource>() }
    singleOf(::UserReactionsStorage) { bind<UserReactionsLocalDataSource>() }
    singleOf(::UserFavoritesStorage) { bind<UserFavoritesLocalDataSource>() }
    singleOf(::UserLibraryStorage) { bind<UserLibraryLocalDataSource>() }
    singleOf(::UserRatingsStorage) { bind<UserRatingsLocalDataSource>() }

    singleOf(::ProgressCompletedStorage) { bind<ProgressCompletedLocalDataSource>() }
    singleOf(::ProgressWatchingStorage) { bind<ProgressWatchingLocalDataSource>() }
    singleOf(::ProgressDroppedStorage) { bind<ProgressDroppedLocalDataSource>() }

    singleOf(::ProfileRatingsStorage) { bind<ProfileRatingsLocalDataSource>() }
    singleOf(::ProfileCommentsStorage) { bind<ProfileCommentsLocalDataSource>() }
    singleOf(::ProfileProfileScreenTimeStorage) { bind<ProfileScreenTimeLocalDataSource>() }

    single<UserWatchlistLocalDataSource> {
        UserWatchlistStorage(
            homeWatchlistStorage = get(clazz = HomeWatchlistLocalDataSource::class),
        )
    }

    single<DataStore<Preferences>>(named(PROFILE_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }
}

internal val profileModule = module {
    singleOf(::ProgressUpdatesStorage) { bind<ProgressUpdates>() }
    singleOf(::SocialUpdatesStorage) { bind<SocialUpdates>() }

    factoryOf(::LoadUserProfileUseCase)
    factoryOf(::GetProgressDroppedUseCase)
    factoryOf(::GetProgressWatchingUseCase)
    factoryOf(::GetProgressCompleteUseCase)
    factoryOf(::GetProfileRatingsUseCase)
    factoryOf(::GetProfileCommentsUseCase)
    factoryOf(::GetProfileStatsUseCase)
    factoryOf(::LoadUserWatchlistUseCase)
    factoryOf(::LoadUserSocialUseCase)
    factoryOf(::LoadUserSocialRequestsUseCase)
    factoryOf(::LoadUserFavoritesUseCase)
    factoryOf(::LoadUserLibraryUseCase)
    factoryOf(::LoadUserListsUseCase)
    factoryOf(::LoadUserLikedListsUseCase)
    factoryOf(::LoadUserProgressUseCase)
    factoryOf(::GetScreenTimeUseCase)
    factoryOf(::LoadUserReactionsUseCase)
    factoryOf(::LoadUserRatingsUseCase)

    factory {
        GetProgressFilterUseCase(
            dataStore = get(named(PROFILE_PREFERENCES)),
        )
    }

    factory {
        GetSocialFilterUseCase(
            dataStore = get(named(PROFILE_PREFERENCES)),
        )
    }

    factory {
        GetActivityFilterUseCase(
            dataStore = get(named(PROFILE_PREFERENCES)),
        )
    }

    factory {
        LogoutUserUseCase(
            appContext = androidApplication(),
            sessionManager = get(),
            collapsingManager = get(),
            checkInManager = get(),
            apiClients = get(named("apiClients")),
            younifyApiClient = get(),
            billingApiClient = get(),
            localUpNext = get(),
            localWatchlistUpNext = get(),
            localUpcoming = get(),
            localSocial = get(),
            localPersonal = get(),
            localListsPersonal = get(),
            localListsLiked = get(),
            localListsCollab = get(),
            localRecommendedShows = get(),
            localRecommendedMovies = get(),
            localUserWatchlist = get(),
            localUserWatchlistMin = get(),
            localUserProgress = get(),
            localUserLists = get(),
            localUserLikedLists = get(),
            localUserFavorites = get(),
            localUserLibrary = get(),
            localUserReactions = get(),
            localUserRatings = get(),
            localProfileDropped = get(),
            localScreenTime = get(),
            localProfileWatching = get(),
            localProfileCompleted = get(),
            localProfileRatings = get(),
            localProfileComments = get(),
            appReviewUseCase = get(),
            analytics = get(),
        )
    }

    viewModelOf(::ProfileViewModel)
    viewModelOf(::ProfileHistoryViewModel)
    viewModelOf(::ProfileFavoritesViewModel)
    viewModelOf(::ProfileLibraryViewModel)
    viewModelOf(::ProfileActivityViewModel)
    viewModelOf(::ProfileScreenTimeViewModel)
    viewModelOf(::ProfileAllActivityViewModel)
    viewModelOf(::ProfileSocialViewModel)
    viewModelOf(::AllProfileSocialViewModel)
    viewModelOf(::ProfileProgressViewModel)

    viewModelOf(::AllProgressViewModel)
    viewModelOf(::AllFavoritesViewModel)
    viewModelOf(::AllLibraryViewModel)

    viewModel { (show: Show) ->
        FavoriteShowContextViewModel(
            show = show,
            sessionManager = get(),
            updateShowFavoritesUseCase = get(),
            userFavoritesLocalSource = get(),
            favoritesUpdates = get(),
            analytics = get(),
        )
    }

    viewModel { (movie: Movie) ->
        FavoriteMovieContextViewModel(
            movie = movie,
            sessionManager = get(),
            updateMovieFavoritesUseCase = get(),
            userFavoritesLocalSource = get(),
            favoritesUpdates = get(),
            analytics = get(),
        )
    }

    viewModel { (data: ScreenTimeData) ->
        ScreenTimeAllViewModel(
            data = data,
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
