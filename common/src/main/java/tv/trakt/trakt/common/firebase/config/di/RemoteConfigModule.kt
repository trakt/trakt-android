package tv.trakt.trakt.common.firebase.config.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.firebase.config.RemoteConfigManager

val remoteConfigModule = module {
    singleOf(::RemoteConfigManager)
}
