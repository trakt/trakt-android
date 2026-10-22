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
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserFavoritesUseCase
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserLibraryUseCase
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserLikedListsUseCase
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserListsUseCase
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.profile.ScreenTimeData
import tv.trakt.trakt.core.auth.di.AUTH_PREFERENCES
import tv.trakt.trakt.core.profile.ProfileViewModel
import tv.trakt.trakt.core.profile.sections.activity.ProfileActivityViewModel
import tv.trakt.trakt.core.profile.sections.activity.all.ProfileAllActivityViewModel
import tv.trakt.trakt.core.profile.sections.activity.data.local.comments.ProfileCommentsLocalSource
import tv.trakt.trakt.core.profile.sections.activity.data.local.ratings.ProfileRatingsLocalSource
import tv.trakt.trakt.core.profile.sections.activity.usecases.GetProfileCommentsUseCase
import tv.trakt.trakt.core.profile.sections.activity.usecases.GetProfileRatingsUseCase
import tv.trakt.trakt.core.profile.sections.activity.usecases.filters.GetActivityFilterUseCase
import tv.trakt.trakt.core.profile.sections.favorites.ProfileFavoritesViewModel
import tv.trakt.trakt.core.profile.sections.favorites.all.AllFavoritesViewModel
import tv.trakt.trakt.core.profile.sections.favorites.context.movie.FavoriteMovieContextViewModel
import tv.trakt.trakt.core.profile.sections.favorites.context.show.FavoriteShowContextViewModel
import tv.trakt.trakt.core.profile.sections.history.ProfileHistoryViewModel
import tv.trakt.trakt.core.profile.sections.leaderboard.LeaderboardViewModel
import tv.trakt.trakt.core.profile.sections.leaderboard.usecases.GetLeaderboardUseCase
import tv.trakt.trakt.core.profile.sections.library.ProfileLibraryViewModel
import tv.trakt.trakt.core.profile.sections.library.all.AllLibraryViewModel
import tv.trakt.trakt.core.profile.sections.library.usecases.GetCollectionLimitServersUseCase
import tv.trakt.trakt.core.profile.sections.progress.ProfileProgressViewModel
import tv.trakt.trakt.core.profile.sections.progress.all.AllProgressViewModel
import tv.trakt.trakt.core.profile.sections.progress.data.local.completed.ProgressCompletedLocalSource
import tv.trakt.trakt.core.profile.sections.progress.data.local.dropped.ProgressDroppedLocalSource
import tv.trakt.trakt.core.profile.sections.progress.data.local.watching.ProgressWatchingLocalSource
import tv.trakt.trakt.core.profile.sections.progress.filters.GetProgressFilterUseCase
import tv.trakt.trakt.core.profile.sections.progress.usecase.GetProgressCompleteUseCase
import tv.trakt.trakt.core.profile.sections.progress.usecase.GetProgressDroppedUseCase
import tv.trakt.trakt.core.profile.sections.progress.usecase.GetProgressWatchingUseCase
import tv.trakt.trakt.core.profile.sections.screentime.ProfileScreenTimeViewModel
import tv.trakt.trakt.core.profile.sections.screentime.all.ScreenTimeAllViewModel
import tv.trakt.trakt.core.profile.sections.screentime.data.local.ProfileScreenTimeLocalSource
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

internal const val PROFILE_PREFERENCES = "profile_preferences_mobile"

internal val profileDataModule = module {
    singleOf(::ProgressCompletedLocalSource)
    singleOf(::ProgressWatchingLocalSource)
    singleOf(::ProgressDroppedLocalSource)

    singleOf(::ProfileRatingsLocalSource)
    singleOf(::ProfileCommentsLocalSource)
    singleOf(::ProfileScreenTimeLocalSource)

    single<DataStore<Preferences>>(named(PROFILE_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }
}

internal val profileModule = module {

    factoryOf(::LoadUserProfileUseCase)
    factoryOf(::GetCollectionLimitServersUseCase)
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
    factoryOf(::GetLeaderboardUseCase)

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
            authPreferences = get(named(AUTH_PREFERENCES)),
            collapsingManager = get(),
            checkInManager = get(),
            apiClients = get(named("apiClients")),
            younifyRemoteSource = get(),
            billingRemoteSource = get(),
            localUpNext = get(),
            localWatchlistUpNext = get(),
            localUpcoming = get(),
            localSocial = get(),
            localPersonal = get(),
            localToday = get(),
            localTodaySeen = get(),
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
            localUserMediaReactions = get(),
            localUserRatings = get(),
            localProfileDropped = get(),
            localScreenTime = get(),
            localLeaderboard = get(),
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
    viewModelOf(::LeaderboardViewModel)
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
            dataChanges = get(),
            analytics = get(),
        )
    }

    viewModel { (movie: Movie) ->
        FavoriteMovieContextViewModel(
            movie = movie,
            sessionManager = get(),
            updateMovieFavoritesUseCase = get(),
            userFavoritesLocalSource = get(),
            dataChanges = get(),
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
