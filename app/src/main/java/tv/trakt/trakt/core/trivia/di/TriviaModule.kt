package tv.trakt.trakt.core.trivia.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.trivia.TriviaViewModel

internal val triviaModule = module {
    viewModel {
        TriviaViewModel(
            savedStateHandle = get(),
            getMovieTriviaUseCase = get(),
            getShowTriviaUseCase = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }
}
