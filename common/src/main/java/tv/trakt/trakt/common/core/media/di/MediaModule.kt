package tv.trakt.trakt.common.core.media.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.media.data.remote.MediaRemoteSource

val mediaDataModule = module {
    singleOf(::MediaRemoteSource)
}
