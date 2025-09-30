package tv.trakt.trakt.core.summary.movies.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieStorage
import tv.trakt.trakt.core.summary.movies.MovieDetailsViewModel
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieDetailsUseCase

internal val movieDetailsDataModule = module {
    single<MovieLocalDataSource> {
        MovieStorage()
    }
}

internal val movieDetailsModule = module {
    factory {
        GetMovieDetailsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    viewModel {
        MovieDetailsViewModel(
            savedStateHandle = get(),
            getMovieDetailsUseCase = get(),
        )
    }
}
