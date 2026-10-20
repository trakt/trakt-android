package tv.trakt.trakt.common.core.translations.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.translations.usecase.GetEpisodeTranslationsUseCase
import tv.trakt.trakt.common.core.translations.usecase.GetMovieTranslationsUseCase
import tv.trakt.trakt.common.core.translations.usecase.GetShowTranslationsUseCase

val translationsModule = module {
    factoryOf(::GetShowTranslationsUseCase)
    factoryOf(::GetMovieTranslationsUseCase)
    factoryOf(::GetEpisodeTranslationsUseCase)
}
