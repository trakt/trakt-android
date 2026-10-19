package tv.trakt.trakt.common.data.local.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.data.local.AllActivityLocalSource
import tv.trakt.trakt.common.data.local.AnticipatedMoviesLocalSource
import tv.trakt.trakt.common.data.local.AnticipatedShowsLocalSource
import tv.trakt.trakt.common.data.local.EpisodeLocalSource
import tv.trakt.trakt.common.data.local.HomeSocialLocalSource
import tv.trakt.trakt.common.data.local.HomeUpNextLocalSource
import tv.trakt.trakt.common.data.local.HomeUpcomingLocalSource
import tv.trakt.trakt.common.data.local.HomeWatchlistLocalSource
import tv.trakt.trakt.common.data.local.LeaderboardLocalSource
import tv.trakt.trakt.common.data.local.ListsCollaborationsLocalSource
import tv.trakt.trakt.common.data.local.ListsLikedLocalSource
import tv.trakt.trakt.common.data.local.ListsPersonalLocalSource
import tv.trakt.trakt.common.data.local.ListsSmartLocalSource
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.PeopleLocalSource
import tv.trakt.trakt.common.data.local.PopularMoviesLocalSource
import tv.trakt.trakt.common.data.local.PopularShowsLocalSource
import tv.trakt.trakt.common.data.local.RecommendedMoviesLocalSource
import tv.trakt.trakt.common.data.local.RecommendedShowsLocalSource
import tv.trakt.trakt.common.data.local.ReleasesMoviesLocalSource
import tv.trakt.trakt.common.data.local.ReleasesShowsLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.local.StreamingLocalSource
import tv.trakt.trakt.common.data.local.TodaySeenLocalSource
import tv.trakt.trakt.common.data.local.TranslationsLocalSource
import tv.trakt.trakt.common.data.local.TrendingMoviesLocalSource
import tv.trakt.trakt.common.data.local.TrendingShowsLocalSource
import tv.trakt.trakt.common.data.local.UserFavoritesLocalSource
import tv.trakt.trakt.common.data.local.UserLibraryLocalSource
import tv.trakt.trakt.common.data.local.UserLikedListsLocalSource
import tv.trakt.trakt.common.data.local.UserListsLocalSource
import tv.trakt.trakt.common.data.local.UserMediaReactionsLocalSource
import tv.trakt.trakt.common.data.local.UserProgressLocalSource
import tv.trakt.trakt.common.data.local.UserRatingsLocalSource
import tv.trakt.trakt.common.data.local.UserReactionsLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
import tv.trakt.trakt.common.core.tutorials.di.createStore as createMigratedStore

// File names predate the move to :common; renaming them would drop stored data.
private const val TODAY_PREFERENCES = "today_preferences_mobile"
private const val MOVIES_PREFERENCES = "movies_preferences"
private const val SHOWS_PREFERENCES = "shows_preferences"

val localSourcesModule = module {
    singleOf(::AllActivityLocalSource)
    singleOf(::EpisodeLocalSource)
    singleOf(::HomeSocialLocalSource)
    singleOf(::HomeUpNextLocalSource)
    singleOf(::HomeUpcomingLocalSource)
    singleOf(::HomeWatchlistLocalSource)
    singleOf(::LeaderboardLocalSource)
    singleOf(::ListsCollaborationsLocalSource)
    singleOf(::ListsLikedLocalSource)
    singleOf(::ListsPersonalLocalSource)
    singleOf(::ListsSmartLocalSource)
    singleOf(::MovieLocalSource)
    singleOf(::PeopleLocalSource)
    singleOf(::ShowLocalSource)
    singleOf(::StreamingLocalSource)
    singleOf(::TranslationsLocalSource)
    singleOf(::UserFavoritesLocalSource)
    singleOf(::UserLibraryLocalSource)
    singleOf(::UserLikedListsLocalSource)
    singleOf(::UserListsLocalSource)
    singleOf(::UserMediaReactionsLocalSource)
    singleOf(::UserProgressLocalSource)
    singleOf(::UserRatingsLocalSource)
    singleOf(::UserReactionsLocalSource)
    singleOf(::UserWatchlistLocalSource)
    singleOf(::UserWatchlistMinimalLocalSource)

    single {
        AnticipatedMoviesLocalSource(
            dataStore = get(named(MOVIES_PREFERENCES)),
        )
    }

    single {
        PopularMoviesLocalSource(
            dataStore = get(named(MOVIES_PREFERENCES)),
        )
    }

    single {
        RecommendedMoviesLocalSource(
            dataStore = get(named(MOVIES_PREFERENCES)),
        )
    }

    single {
        ReleasesMoviesLocalSource(
            dataStore = get(named(MOVIES_PREFERENCES)),
        )
    }

    single {
        TrendingMoviesLocalSource(
            dataStore = get(named(MOVIES_PREFERENCES)),
        )
    }

    single {
        AnticipatedShowsLocalSource(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single {
        PopularShowsLocalSource(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single {
        RecommendedShowsLocalSource(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single {
        ReleasesShowsLocalSource(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single {
        TrendingShowsLocalSource(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single<DataStore<Preferences>>(named(MOVIES_PREFERENCES)) {
        createMigratedStore(
            context = androidApplication(),
            key = MOVIES_PREFERENCES,
        )
    }

    single<DataStore<Preferences>>(named(SHOWS_PREFERENCES)) {
        createMigratedStore(
            context = androidApplication(),
            key = SHOWS_PREFERENCES,
        )
    }

    single {
        TodaySeenLocalSource(
            dataStore = get(named(TODAY_PREFERENCES)),
        )
    }

    single<DataStore<Preferences>>(named(TODAY_PREFERENCES)) {
        createStore(
            context = androidApplication(),
            name = TODAY_PREFERENCES,
        )
    }
}

private fun createStore(
    context: Context,
    name: String,
): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(name) },
    )
}
