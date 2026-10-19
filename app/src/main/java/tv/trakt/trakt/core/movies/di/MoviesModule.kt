package tv.trakt.trakt.core.movies.di

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
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.movies.AnticipatedMoviesLocalSource
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.movies.DefaultGetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.popular.data.local.movies.PopularMoviesLocalSource
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularMoviesUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.movies.DefaultGetPopularMoviesUseCase
import tv.trakt.trakt.core.discover.sections.releases.data.local.movies.ReleasesMoviesLocalSource
import tv.trakt.trakt.core.discover.sections.releases.usecases.movies.DefaultGetReleasesMoviesUseCase
import tv.trakt.trakt.core.discover.sections.releases.usecases.movies.GetReleasesMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.data.local.movies.TrendingMoviesLocalSource
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.movies.DefaultGetTrendingMoviesUseCase
import tv.trakt.trakt.core.home.sections.recommended.data.movies.RecommendedMoviesLocalSource
import tv.trakt.trakt.core.home.sections.recommended.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.HideRecommendedMovieUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.movies.DefaultGetRecommendedMoviesUseCase
import tv.trakt.trakt.core.movies.ui.context.MovieContextViewModel

private const val MOVIES_PREFERENCES = "movies_preferences"

internal val moviesDataModule = module {
    single {
        PopularMoviesLocalSource(
            dataStore = get(named(MOVIES_PREFERENCES)),
        )
    }

    single {
        ReleasesMoviesLocalSource(
            dataStore = get(named(MOVIES_PREFERENCES)),
        )
    }

    single {
        RecommendedMoviesLocalSource(
            dataStore = get(named(MOVIES_PREFERENCES)),
        )
    }

    single {
        AnticipatedMoviesLocalSource(
            dataStore = get(named(MOVIES_PREFERENCES)),
        )
    }

    single {
        TrendingMoviesLocalSource(
            dataStore = get(named(MOVIES_PREFERENCES)),
        )
    }

    single<DataStore<Preferences>>(named(MOVIES_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }
}

internal val moviesModule = module {
    factory<GetTrendingMoviesUseCase>(
        qualifier = named("defaultTrendingMoviesUseCase"),
    ) {
        DefaultGetTrendingMoviesUseCase(
            remoteSource = get(),
            localTrendingSource = get(),
            localMovieSource = get(),
            seasonalThemeUseCase = get(),
        )
    }

    factory<GetPopularMoviesUseCase>(
        qualifier = named("defaultPopularMoviesUseCase"),
    ) {
        DefaultGetPopularMoviesUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localMovieSource = get(),
            seasonalThemeUseCase = get(),
        )
    }

    factory<GetAnticipatedMoviesUseCase>(
        qualifier = named("defaultAnticipatedMoviesUseCase"),
    ) {
        DefaultGetAnticipatedMoviesUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localMovieSource = get(),
            seasonalThemeUseCase = get(),
        )
    }

    factory<GetReleasesMoviesUseCase>(
        qualifier = named("defaultReleasesMoviesUseCase"),
    ) {
        DefaultGetReleasesMoviesUseCase(
            remoteSource = get(),
            localSource = get(),
            localMovieSource = get(),
        )
    }

    factory<GetRecommendedMoviesUseCase>(
        qualifier = named("defaultRecommendedMoviesUseCase"),
    ) {
        DefaultGetRecommendedMoviesUseCase(
            remoteSource = get(),
            localRecommendedSource = get(),
            localMovieSource = get(),
        )
    }

    factoryOf(::HideRecommendedMovieUseCase)

    viewModel { (movie: Movie) ->
        MovieContextViewModel(
            appContext = androidApplication(),
            movie = movie,
            updateMovieHistoryUseCase = get(),
            updateMovieWatchlistUseCase = get(),
            userProgressLocalSource = get(),
            userWatchlistLocalSource = get(),
            userWatchlistMinLocalSource = get(),
            loadProgressUseCase = get(),
            loadWatchlistUseCase = get(),
            sessionManager = get(),
            checkInManager = get(),
            ratePromptManager = get(),
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
        migrations = listOf(SharedPreferencesMigration(context, MOVIES_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(MOVIES_PREFERENCES) },
    )
}
