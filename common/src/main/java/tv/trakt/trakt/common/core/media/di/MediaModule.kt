package tv.trakt.trakt.common.core.media.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.media.data.remote.MediaApiClient
import tv.trakt.trakt.common.core.media.data.remote.MediaRemoteDataSource

val mediaDataModule = module {
    singleOf(::MediaApiClient) { bind<MediaRemoteDataSource>() }
}
