package tv.trakt.trakt.core.applinks.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.applinks.AppLinkViewModel

internal val appLinksModule = module {
    viewModelOf(::AppLinkViewModel)
}
