package tv.trakt.trakt.core.streamings.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.streamings.AllStreamingsViewModel

internal val allStreamingsModule = module {
    viewModelOf(::AllStreamingsViewModel)
}
