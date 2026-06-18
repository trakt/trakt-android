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
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.movies.AnticipatedMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.movies.AnticipatedMoviesStorage
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.movies.CustomGetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.movies.DefaultGetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.popular.data.local.movies.PopularMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.popular.data.local.movies.PopularMoviesStorage
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularMoviesUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.movies.CustomGetPopularMoviesUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.movies.DefaultGetPopularMoviesUseCase
import tv.trakt.trakt.core.discover.sections.recommended.data.local.movies.RecommendedMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.recommended.data.local.movies.RecommendedMoviesStorage
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.recommended.usecase.movies.CustomGetRecommendedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.recommended.usecase.movies.DefaultGetRecommendedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.data.local.movies.TrendingMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.trending.data.local.movies.TrendingMoviesStorage
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.movies.DefaultGetTrendingMoviesUseCase
import tv.trakt.trakt.core.movies.data.remote.MoviesApiClient
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.ui.context.MovieContextViewModel

private const val MOVIES_PREFERENCES = "movies_preferences"

internal val moviesDataModule = module {

    singleOf(::MoviesApiClient) { bind<MoviesRemoteDataSource>() }

    single<PopularMoviesLocalDataSource> {
        PopularMoviesStorage(
            dataStore = get(named(MOVIES_PREFERENCES)),
        )
    }

    single<RecommendedMoviesLocalDataSource> {
        RecommendedMoviesStorage(
            dataStore = get(named(MOVIES_PREFERENCES)),
        )
    }

    single<AnticipatedMoviesLocalDataSource> {
        AnticipatedMoviesStorage(
            dataStore = get(named(MOVIES_PREFERENCES)),
        )
    }

    single<TrendingMoviesLocalDataSource> {
        TrendingMoviesStorage(
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
        )
    }

//    factory<GetTrendingMoviesUseCase>(
//        qualifier = named("customTrendingMoviesUseCase"),
//    ) {
//        CustomGetTrendingMoviesUseCase(
//            remoteSource = get(),
//            localTrendingSource = get(),
//            localMovieSource = get(),
//            customThemeUseCase = get(),
//        )
//    }

    factory<GetPopularMoviesUseCase>(
        qualifier = named("defaultPopularMoviesUseCase"),
    ) {
        DefaultGetPopularMoviesUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localMovieSource = get(),
        )
    }

    factory<GetPopularMoviesUseCase>(
        qualifier = named("customPopularMoviesUseCase"),
    ) {
        CustomGetPopularMoviesUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localMovieSource = get(),
            customThemeUseCase = get(),
        )
    }

    factory<GetAnticipatedMoviesUseCase>(
        qualifier = named("defaultAnticipatedMoviesUseCase"),
    ) {
        DefaultGetAnticipatedMoviesUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localMovieSource = get(),
        )
    }

    factory<GetAnticipatedMoviesUseCase>(
        qualifier = named("customAnticipatedMoviesUseCase"),
    ) {
        CustomGetAnticipatedMoviesUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localMovieSource = get(),
            customThemeUseCase = get(),
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

    factory<GetRecommendedMoviesUseCase>(
        qualifier = named("customRecommendedMoviesUseCase"),
    ) {
        CustomGetRecommendedMoviesUseCase(
            remoteSource = get(),
            localRecommendedSource = get(),
            localMovieSource = get(),
            customThemeUseCase = get(),
        )
    }

    viewModel { (movie: Movie) ->
        MovieContextViewModel(
            appContext = androidApplication(),
            movie = movie,
            updateMovieHistoryUseCase = get(),
            updateMovieWatchlistUseCase = get(),
            userProgressLocalSource = get(),
            userWatchlistLocalSource = get(),
            userWatchlistMinLocalDataSource = get(),
            loadProgressUseCase = get(),
            loadWatchlistUseCase = get(),
            sessionManager = get(),
            checkInManager = get(),
            ratePromptManager = get(),
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
        migrations = listOf(SharedPreferencesMigration(context, MOVIES_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(MOVIES_PREFERENCES) },
    )
}
