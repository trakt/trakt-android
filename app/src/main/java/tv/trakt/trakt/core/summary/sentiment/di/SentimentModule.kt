package tv.trakt.trakt.core.summary.sentiment.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.summary.sentiment.SentimentViewModel
import tv.trakt.trakt.core.summary.sentiment.usecases.GetMockSentimentUseCase

internal val sentimentModule = module {
    factoryOf(::GetMockSentimentUseCase)

    viewModelOf(::SentimentViewModel)
}
