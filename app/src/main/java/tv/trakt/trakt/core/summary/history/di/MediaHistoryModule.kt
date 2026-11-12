package tv.trakt.trakt.core.summary.history.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.summary.history.MediaHistoryViewModel
import tv.trakt.trakt.core.summary.history.usecases.GetMediaHistoryUseCase

internal val mediaHistoryModule = module {
    factoryOf(::GetMediaHistoryUseCase)
    viewModelOf(::MediaHistoryViewModel)
}
