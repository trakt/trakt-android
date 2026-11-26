package tv.trakt.trakt.core.mixtape.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.discover.di.DISCOVER_PREFERENCES
import tv.trakt.trakt.core.mixtape.MixtapeViewModel
import tv.trakt.trakt.core.mixtape.usecases.GetMixtapeHistoryUseCase
import tv.trakt.trakt.core.mixtape.usecases.GetMixtapeUseCase
import tv.trakt.trakt.core.mixtape.usecases.GetMixtapeVectorsUseCase
import tv.trakt.trakt.core.mixtape.usecases.MixtapeHintUseCase
import tv.trakt.trakt.core.mixtape.usecases.ScoreMixtapeFitsUseCase

internal val mixtapeModule = module {
    factory {
        MixtapeHintUseCase(
            dataStore = get(named(DISCOVER_PREFERENCES)),
        )
    }

    factoryOf(::GetMixtapeHistoryUseCase)
    factoryOf(::GetMixtapeVectorsUseCase)
    factoryOf(::GetMixtapeUseCase)
    factoryOf(::ScoreMixtapeFitsUseCase)

    viewModelOf(::MixtapeViewModel)
}
