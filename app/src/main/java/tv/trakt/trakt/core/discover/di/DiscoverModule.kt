package tv.trakt.trakt.core.discover.di

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
import tv.trakt.trakt.core.discover.DiscoverViewModel
import tv.trakt.trakt.core.discover.data.local.media.DiscoverMediaLocalSource
import tv.trakt.trakt.core.discover.sections.all.AllDiscoverViewModel
import tv.trakt.trakt.core.discover.sections.all.usecases.GetAllDiscoverMoviesUseCase
import tv.trakt.trakt.core.discover.sections.all.usecases.GetAllDiscoverShowsUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.DiscoverAnticipatedViewModel
import tv.trakt.trakt.core.discover.sections.popular.DiscoverPopularViewModel
import tv.trakt.trakt.core.discover.sections.releases.DiscoverReleasesViewModel
import tv.trakt.trakt.core.discover.sections.releases.usecases.GetReleasesTypeUseCase
import tv.trakt.trakt.core.discover.sections.trending.DiscoverTrendingViewModel
import tv.trakt.trakt.core.discover.usecases.GetDiscoverMediaUseCase

internal const val DISCOVER_PREFERENCES = "discover_preferences_mobile"

internal val discoverModule = module {

    single<DataStore<Preferences>>(named(DISCOVER_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }

    single {
        DiscoverMediaLocalSource(
            dataStore = get(named(DISCOVER_PREFERENCES)),
        )
    }

    factoryOf(::GetDiscoverMediaUseCase)

    factory {
        GetReleasesTypeUseCase(
            dataStore = get(named(DISCOVER_PREFERENCES)),
        )
    }

    factory(
        qualifier = named("defaultAllDiscoverShowsUseCase"),
    ) {
        GetAllDiscoverShowsUseCase(
            getTrendingShowsUseCase = get(named("defaultTrendingShowsUseCase")),
            getAnticipatedShowsUseCase = get(named("defaultAnticipatedShowsUseCase")),
            getPopularShowsUseCase = get(named("defaultPopularShowsUseCase")),
            getRecommendedShowsUseCase = get(named("defaultRecommendedShowsUseCase")),
        )
    }

    factory(
        qualifier = named("defaultAllDiscoverMoviesUseCase"),
    ) {
        GetAllDiscoverMoviesUseCase(
            getTrendingMoviesUseCase = get(named("defaultTrendingMoviesUseCase")),
            getAnticipatedMoviesUseCase = get(named("defaultAnticipatedMoviesUseCase")),
            getPopularMoviesUseCase = get(named("defaultPopularMoviesUseCase")),
            getRecommendedMoviesUseCase = get(named("defaultRecommendedMoviesUseCase")),
        )
    }

    viewModel {
        DiscoverViewModel(
            sessionManager = get(),
            analytics = get(),
            collectionStateProvider = get(),
            getCalendarViewUseCase = get(),
        )
    }

    viewModel {
        AllDiscoverViewModel(
            savedStateHandle = get(),
            analytics = get(),
            filterManager = get(),
            sessionManager = get(),
            getShowsUseCase = get(named("defaultAllDiscoverShowsUseCase")),
            getMoviesUseCase = get(named("defaultAllDiscoverMoviesUseCase")),
            getDiscoverMediaUseCase = get(),
            hideRecommendedShowUseCase = get(),
            hideRecommendedMovieUseCase = get(),
            collectionStateProvider = get(),
            seasonalThemeUseCase = get(),
        )
    }

    viewModel {
        DiscoverTrendingViewModel(
            filterManager = get(),
            collapsingManager = get(),
            getTrendingShowsUseCase = get(named("defaultTrendingShowsUseCase")),
            getTrendingMoviesUseCase = get(named("defaultTrendingMoviesUseCase")),
            getDiscoverMediaUseCase = get(),
            seasonalThemeUseCase = get(),
            appLifecycleProvider = get(),
        )
    }

    viewModel {
        DiscoverAnticipatedViewModel(
            filterManager = get(),
            collapsingManager = get(),
            getAnticipatedShowsUseCase = get(named("defaultAnticipatedShowsUseCase")),
            getAnticipatedMoviesUseCase = get(named("defaultAnticipatedMoviesUseCase")),
            getDiscoverMediaUseCase = get(),
            seasonalThemeUseCase = get(),
            appLifecycleProvider = get(),
        )
    }

    viewModel {
        DiscoverPopularViewModel(
            filterManager = get(),
            collapsingManager = get(),
            getPopularShowsUseCase = get(named("defaultPopularShowsUseCase")),
            getPopularMoviesUseCase = get(named("defaultPopularMoviesUseCase")),
            getDiscoverMediaUseCase = get(),
            seasonalThemeUseCase = get(),
            appLifecycleProvider = get(),
        )
    }

    viewModel {
        DiscoverReleasesViewModel(
            filterManager = get(),
            collapsingManager = get(),
            getReleasesShowsUseCase = get(named("defaultReleasesShowsUseCase")),
            getReleasesMoviesUseCase = get(named("defaultReleasesMoviesUseCase")),
            getReleasesTypeUseCase = get(),
            appLifecycleProvider = get(),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, DISCOVER_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(DISCOVER_PREFERENCES) },
    )
}
