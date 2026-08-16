package tv.trakt.trakt.common.core.klipy.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.klipy.data.remote.GifsApiClient
import tv.trakt.trakt.common.core.klipy.data.remote.GifsRemoteDataSource

val gifsDataModule = module {
    singleOf(::GifsApiClient) { bind<GifsRemoteDataSource>() }
}
