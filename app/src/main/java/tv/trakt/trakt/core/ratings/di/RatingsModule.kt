package tv.trakt.trakt.core.ratings.di

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
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.ratings.DeleteRatingUseCase
import tv.trakt.trakt.core.ratings.PostRatingUseCase
import tv.trakt.trakt.core.ratings.data.RatingsUpdates
import tv.trakt.trakt.core.ratings.data.RatingsUpdatesStorage
import tv.trakt.trakt.core.ratings.data.remote.RatingsApiClient
import tv.trakt.trakt.core.ratings.data.remote.RatingsRemoteDataSource
import tv.trakt.trakt.core.ratings.data.work.PostRatingWorker
import tv.trakt.trakt.core.ratings.rateprompt.DefaultRatePromptManager
import tv.trakt.trakt.core.ratings.rateprompt.RatePromptManager
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptMedia
import tv.trakt.trakt.core.ratings.rateprompt.ui.RatePromptViewModel

internal const val RATINGS_PREFERENCES = "ratings_preferences"

internal val ratingsDataModule = module {
    single<RatingsRemoteDataSource> {
        RatingsApiClient(
            ratingsApi = get(),
            cacheMarker = get(),
        )
    }

    single<DataStore<Preferences>>(named(RATINGS_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }

    single<RatingsUpdates> {
        RatingsUpdatesStorage()
    }

    single<RatePromptManager> {
        DefaultRatePromptManager(
            dataStore = get(named(RATINGS_PREFERENCES)),
            sessionManager = get(),
            checkInManager = get(),
            userRatingsUseCase = get(),
            userFavoritesUseCase = get(),
            userHistoryDataSource = get(),
        )
    }
}

internal val ratingsModule = module {
    viewModel { (media: RatePromptMedia, moreMedia: List<RatePromptMedia>) ->
        RatePromptViewModel(
            media = media,
            moreMedia = moreMedia,
            appContext = androidApplication(),
            sessionManager = get(),
            ratePromptManager = get(),
            updateMovieFavoritesUseCase = get(),
            userFavoritesLocalSource = get(),
            favoritesUpdates = get(),
            analytics = get(),
        )
    }

    factory {
        PostRatingUseCase(
            remoteSource = get(),
        )
    }

    factory {
        DeleteRatingUseCase(
            remoteSource = get(),
        )
    }

    worker {
        PostRatingWorker(
            appContext = androidApplication(),
            workerParams = get(),
            sessionManager = get(),
            postRatingUseCase = get(),
            deleteRatingUseCase = get(),
            loadUserRatingUseCase = get(),
            ratingsUpdates = get(),
            analytics = get(),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, RATINGS_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(RATINGS_PREFERENCES) },
    )
}
