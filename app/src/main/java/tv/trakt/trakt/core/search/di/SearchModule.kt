package tv.trakt.trakt.core.search.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.search.features.game.GameViewModel

internal val searchModule = module {
    viewModelOf(::GameViewModel)
}
