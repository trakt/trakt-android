package tv.trakt.trakt.core.mixtape.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.discover.di.DISCOVER_PREFERENCES
import tv.trakt.trakt.core.mixtape.MixtapeViewModel
import tv.trakt.trakt.core.mixtape.usecases.MixtapeHintUseCase

internal val mixtapeModule = module {
    factory {
        MixtapeHintUseCase(
            dataStore = get(named(DISCOVER_PREFERENCES)),
        )
    }

    viewModelOf(::MixtapeViewModel)
}
