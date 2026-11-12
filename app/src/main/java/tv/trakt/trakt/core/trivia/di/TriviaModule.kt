package tv.trakt.trakt.core.trivia.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.trivia.MediaTriviaViewModel
import tv.trakt.trakt.core.trivia.TriviaViewModel
import tv.trakt.trakt.core.trivia.usecases.GetTriviaUseCase

internal val triviaModule = module {
    factoryOf(::GetTriviaUseCase)

    viewModelOf(::TriviaViewModel)

    viewModel { (mediaId: TraktId, mediaType: MediaType) ->
        MediaTriviaViewModel(
            mediaId = mediaId,
            mediaType = mediaType,
            getTriviaUseCase = get(),
            sessionManager = get(),
            analytics = get(),
            collapsingManager = get(),
        )
    }
}
