package tv.trakt.trakt.core.trivia.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.trivia.TriviaViewModel

internal val triviaModule = module {
    viewModel { (mediaId: TraktId, mediaType: MediaType, backgroundUrl: String?) ->
        TriviaViewModel(
            mediaId = mediaId,
            mediaType = mediaType,
            backgroundUrl = backgroundUrl,
            getMovieTriviaUseCase = get(),
            getShowTriviaUseCase = get(),
            sessionManager = get(),
        )
    }
}
