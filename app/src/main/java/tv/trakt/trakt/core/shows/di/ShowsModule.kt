package tv.trakt.trakt.core.shows.di

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
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.shows.AnticipatedShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.shows.AnticipatedShowsStorage
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.shows.CustomGetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.shows.DefaultGetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.data.local.shows.PopularShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.popular.data.local.shows.PopularShowsStorage
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.shows.CustomGetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.shows.DefaultGetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.recommended.data.local.shows.RecommendedShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.recommended.data.local.shows.RecommendedShowsStorage
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.discover.sections.recommended.usecase.shows.CustomGetRecommendedShowsUseCase
import tv.trakt.trakt.core.discover.sections.recommended.usecase.shows.DefaultGetRecommendedShowsUseCase
import tv.trakt.trakt.core.discover.sections.releases.data.local.shows.ReleasesShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.releases.data.local.shows.ReleasesShowsStorage
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.DefaultGetReleasesShowsUseCase
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.GetReleasesShowsUseCase
import tv.trakt.trakt.core.discover.sections.trending.data.local.shows.TrendingShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.trending.data.local.shows.TrendingShowsStorage
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingShowsUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.shows.DefaultGetTrendingShowsUseCase
import tv.trakt.trakt.core.episodes.data.remote.EpisodesApiClient
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.core.shows.data.remote.ShowsApiClient
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.ui.context.ShowContextViewModel

private const val SHOWS_PREFERENCES = "shows_preferences"

internal val showsDataModule = module {
    singleOf(::ShowsApiClient) { bind<ShowsRemoteDataSource>() }

    singleOf(::EpisodesApiClient) { bind<EpisodesRemoteDataSource>() }

    single<TrendingShowsLocalDataSource> {
        TrendingShowsStorage(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single<ReleasesShowsLocalDataSource> {
        ReleasesShowsStorage(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single<RecommendedShowsLocalDataSource> {
        RecommendedShowsStorage(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single<PopularShowsLocalDataSource> {
        PopularShowsStorage(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single<AnticipatedShowsLocalDataSource> {
        AnticipatedShowsStorage(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single<ReleasesShowsLocalDataSource> {
        ReleasesShowsStorage(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single<DataStore<Preferences>>(named(SHOWS_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }
}

internal val showsModule = module {
    factory<GetTrendingShowsUseCase>(
        qualifier = named("defaultTrendingShowsUseCase"),
    ) {
        DefaultGetTrendingShowsUseCase(
            remoteSource = get(),
            localTrendingSource = get(),
            localShowSource = get(),
        )
    }

//    factory<GetTrendingShowsUseCase>(
//        qualifier = named("customTrendingShowsUseCase"),
//    ) {
//        CustomGetTrendingShowsUseCase(
//            remoteSource = get(),
//            localTrendingSource = get(),
//            localShowSource = get(),
//            customThemeUseCase = get(),
//        )
//    }

    factory<GetPopularShowsUseCase>(
        qualifier = named("defaultPopularShowsUseCase"),
    ) {
        DefaultGetPopularShowsUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localShowSource = get(),
        )
    }

    factory<GetPopularShowsUseCase>(
        qualifier = named("customPopularShowsUseCase"),
    ) {
        CustomGetPopularShowsUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localShowSource = get(),
            customThemeUseCase = get(),
        )
    }

    factory<GetAnticipatedShowsUseCase>(
        qualifier = named("defaultAnticipatedShowsUseCase"),
    ) {
        DefaultGetAnticipatedShowsUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localShowSource = get(),
        )
    }

    factory<GetAnticipatedShowsUseCase>(
        qualifier = named("customAnticipatedShowsUseCase"),
    ) {
        CustomGetAnticipatedShowsUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localShowSource = get(),
            customThemeUseCase = get(),
        )
    }

    factory<GetReleasesShowsUseCase>(
        qualifier = named("defaultReleasesShowsUseCase"),
    ) {
        DefaultGetReleasesShowsUseCase(
            remoteSource = get(),
            localSource = get(),
            localShowSource = get(),
            localEpisodeSource = get(),
        )
    }

    factory<GetRecommendedShowsUseCase>(
        qualifier = named("defaultRecommendedShowsUseCase"),
    ) {
        DefaultGetRecommendedShowsUseCase(
            remoteSource = get(),
            localRecommendedSource = get(),
            localShowSource = get(),
        )
    }

    factory<GetRecommendedShowsUseCase>(
        qualifier = named("customRecommendedShowsUseCase"),
    ) {
        CustomGetRecommendedShowsUseCase(
            remoteSource = get(),
            localRecommendedSource = get(),
            localShowSource = get(),
            customThemeUseCase = get(),
        )
    }

    viewModel { (show: Show) ->
        ShowContextViewModel(
            show = show,
            updateWatchlistUseCase = get(),
            updateHistoryUseCase = get(),
            userProgressLocalSource = get(),
            userWatchlistLocalSource = get(),
            userWatchlistMinLocalSource = get(),
            loadProgressUseCase = get(),
            loadWatchlistUseCase = get(),
            sessionManager = get(),
            analytics = get(),
            errorsManager = get(),
            watchlistUpdates = get(),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, SHOWS_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(SHOWS_PREFERENCES) },
    )
}
