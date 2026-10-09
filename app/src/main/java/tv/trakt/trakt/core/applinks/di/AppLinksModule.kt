package tv.trakt.trakt.core.applinks.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.applinks.AppLinkViewModel
import tv.trakt.trakt.core.applinks.ShareArrivalEvents
import tv.trakt.trakt.core.applinks.usecases.RecordShareArrivalUseCase
import tv.trakt.trakt.core.applinks.usecases.ResolveImdbLinkUseCase

internal val appLinksModule = module {
    singleOf(::ShareArrivalEvents)
    factoryOf(::ResolveImdbLinkUseCase)
    factoryOf(::RecordShareArrivalUseCase)
    viewModelOf(::AppLinkViewModel)
}
