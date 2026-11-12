package tv.trakt.trakt.core.summary.sentiment.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.sentiment.MediaSentimentViewModel
import tv.trakt.trakt.core.summary.sentiment.SentimentViewModel
import tv.trakt.trakt.core.summary.sentiment.usecases.GetMockSentimentUseCase
import tv.trakt.trakt.core.summary.sentiment.usecases.GetRealityCheckUseCase
import tv.trakt.trakt.core.summary.sentiment.usecases.GetSentimentUseCase

internal val sentimentModule = module {
    factoryOf(::GetMockSentimentUseCase)
    factoryOf(::GetSentimentUseCase)
    factoryOf(::GetRealityCheckUseCase)

    viewModelOf(::SentimentViewModel)

    viewModel { (mediaId: TraktId, mediaType: MediaType) ->
        MediaSentimentViewModel(
            mediaId = mediaId,
            mediaType = mediaType,
            getSentimentUseCase = get(),
            getRealityCheckUseCase = get(),
            sessionManager = get(),
            collapsingManager = get(),
        )
    }
}
