package tv.trakt.trakt.common.core.klipy.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.klipy.data.remote.GifsRemoteSource

val gifsDataModule = module {
    singleOf(::GifsRemoteSource)
}
