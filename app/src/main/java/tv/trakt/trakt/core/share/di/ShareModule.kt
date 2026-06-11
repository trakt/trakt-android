package tv.trakt.trakt.core.share.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.share.ShareViewModel

internal val shareModule = module {
    viewModelOf(::ShareViewModel)
}
