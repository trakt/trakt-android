package tv.trakt.trakt.common.core.translations.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.translations.usecase.GetLocalizedTitlesUseCase

val translationsModule = module {
    factoryOf(::GetLocalizedTitlesUseCase)
}
