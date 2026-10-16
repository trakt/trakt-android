package tv.trakt.trakt.common.core.plex.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.plex.data.remote.PlexApiClient
import tv.trakt.trakt.common.core.plex.data.remote.PlexRemoteDataSource

val plexRemoteModule = module {
    singleOf(::PlexApiClient) { bind<PlexRemoteDataSource>() }
}
