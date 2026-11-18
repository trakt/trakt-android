package tv.trakt.trakt.core.featureflags.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.featureflags.usecases.FeatureFlagsUseCase
import tv.trakt.trakt.core.settings.di.SETTINGS_PREFERENCES

internal val featureFlagsModule = module {
    factory {
        FeatureFlagsUseCase(
            sessionManager = get(),
            dataStore = get(named(SETTINGS_PREFERENCES)),
        )
    }
}
