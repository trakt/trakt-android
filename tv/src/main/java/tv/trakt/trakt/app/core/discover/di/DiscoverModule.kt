package tv.trakt.trakt.app.core.discover.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.app.core.discover.DiscoverViewModel
import tv.trakt.trakt.app.core.discover.features.media.DiscoverMediaViewAllViewModel
import tv.trakt.trakt.app.core.discover.features.releases.DiscoverReleasesViewAllViewModel
import tv.trakt.trakt.app.core.discover.usecase.GetDiscoverMediaUseCase
import tv.trakt.trakt.app.core.discover.usecase.GetDiscoverSectionsUseCase

internal val discoverModule = module {
    factoryOf(::GetDiscoverMediaUseCase)
    factoryOf(::GetDiscoverSectionsUseCase)

    viewModelOf(::DiscoverViewModel)
    viewModelOf(::DiscoverMediaViewAllViewModel)
    viewModelOf(::DiscoverReleasesViewAllViewModel)
}
