package tv.trakt.trakt.common.core.translations.di

import org.koin.dsl.module
import tv.trakt.trakt.common.core.translations.data.remote.TranslationsApiClient
import tv.trakt.trakt.common.core.translations.data.remote.TranslationsRemoteDataSource
import tv.trakt.trakt.common.core.translations.usecase.GetEpisodeTranslationsUseCase
import tv.trakt.trakt.common.core.translations.usecase.GetMovieTranslationsUseCase
import tv.trakt.trakt.common.core.translations.usecase.GetShowTranslationsUseCase

val translationsDataModule = module {
    single<TranslationsRemoteDataSource> {
        TranslationsApiClient(
            showsApi = get(),
            moviesApi = get(),
        )
    }
}

val translationsModule = module {
    factory {
        GetShowTranslationsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetMovieTranslationsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetEpisodeTranslationsUseCase(
            remoteSource = get(),
        )
    }
}
