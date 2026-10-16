package tv.trakt.trakt.common.core.sync.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.sync.data.remote.episodes.EpisodesSyncRemoteSource
import tv.trakt.trakt.common.core.sync.data.remote.movies.MoviesSyncRemoteSource
import tv.trakt.trakt.common.core.sync.data.remote.shows.ShowsSyncRemoteSource

val syncRemoteModule = module {
    singleOf(::ShowsSyncRemoteSource)
    singleOf(::MoviesSyncRemoteSource)
    singleOf(::EpisodesSyncRemoteSource)
}
