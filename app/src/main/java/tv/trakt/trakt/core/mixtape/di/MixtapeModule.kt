package tv.trakt.trakt.core.mixtape.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.mixtape.MixtapeViewModel

internal val mixtapeModule = module {
    viewModelOf(::MixtapeViewModel)
}
