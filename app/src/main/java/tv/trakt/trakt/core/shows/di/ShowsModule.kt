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
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.shows.AnticipatedShowsLocalSource
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.shows.DefaultGetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.data.local.shows.PopularShowsLocalSource
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.shows.DefaultGetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.releases.data.local.shows.ReleasesShowsLocalSource
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.DefaultGetReleasesShowsUseCase
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.GetReleasesShowsUseCase
import tv.trakt.trakt.core.discover.sections.trending.data.local.shows.TrendingShowsLocalSource
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingShowsUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.shows.DefaultGetTrendingShowsUseCase
import tv.trakt.trakt.core.home.sections.recommended.data.shows.RecommendedShowsLocalSource
import tv.trakt.trakt.core.home.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.HideRecommendedShowUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.shows.DefaultGetRecommendedShowsUseCase
import tv.trakt.trakt.core.shows.ui.context.ShowContextViewModel

private const val SHOWS_PREFERENCES = "shows_preferences"

internal val showsDataModule = module {
    single {
        TrendingShowsLocalSource(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single {
        ReleasesShowsLocalSource(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single {
        RecommendedShowsLocalSource(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single {
        PopularShowsLocalSource(
            dataStore = get(named(SHOWS_PREFERENCES)),
        )
    }

    single {
        AnticipatedShowsLocalSource(
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
            seasonalThemeUseCase = get(),
        )
    }

    factory<GetPopularShowsUseCase>(
        qualifier = named("defaultPopularShowsUseCase"),
    ) {
        DefaultGetPopularShowsUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localShowSource = get(),
            seasonalThemeUseCase = get(),
        )
    }

    factory<GetAnticipatedShowsUseCase>(
        qualifier = named("defaultAnticipatedShowsUseCase"),
    ) {
        DefaultGetAnticipatedShowsUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localShowSource = get(),
            seasonalThemeUseCase = get(),
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

    factoryOf(::HideRecommendedShowUseCase)

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
            dataChanges = get(),
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
