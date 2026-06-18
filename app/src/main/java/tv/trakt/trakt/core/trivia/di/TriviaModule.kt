package tv.trakt.trakt.core.trivia.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.trivia.TriviaViewModel

internal val triviaModule = module {
    viewModelOf(::TriviaViewModel)
}
