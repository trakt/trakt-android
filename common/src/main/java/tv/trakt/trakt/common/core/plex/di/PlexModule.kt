package tv.trakt.trakt.common.core.plex.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.plex.data.remote.PlexRemoteSource

val plexRemoteModule = module {
    singleOf(::PlexRemoteSource)
}
