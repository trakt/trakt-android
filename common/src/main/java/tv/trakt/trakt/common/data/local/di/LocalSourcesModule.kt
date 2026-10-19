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
import tv.trakt.trakt.common.data.local.EpisodeLocalSource
import tv.trakt.trakt.common.data.local.HomeUpNextLocalSource
import tv.trakt.trakt.common.data.local.HomeWatchlistLocalSource
import tv.trakt.trakt.common.data.local.LeaderboardLocalSource
import tv.trakt.trakt.common.data.local.ListsCollaborationsLocalSource
import tv.trakt.trakt.common.data.local.ListsLikedLocalSource
import tv.trakt.trakt.common.data.local.ListsPersonalLocalSource
import tv.trakt.trakt.common.data.local.ListsSmartLocalSource
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.PeopleLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.local.StreamingLocalSource
import tv.trakt.trakt.common.data.local.TodaySeenLocalSource
import tv.trakt.trakt.common.data.local.TranslationsLocalSource
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

// File name predates the move to :common; renaming it would drop stored data.
private const val TODAY_PREFERENCES = "today_preferences_mobile"

val localSourcesModule = module {
    singleOf(::AllActivityLocalSource)
    singleOf(::EpisodeLocalSource)
    singleOf(::HomeUpNextLocalSource)
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
