package tv.trakt.trakt.common.core.sync.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.sync.data.remote.episodes.EpisodesSyncApiClient
import tv.trakt.trakt.common.core.sync.data.remote.episodes.EpisodesSyncRemoteDataSource
import tv.trakt.trakt.common.core.sync.data.remote.movies.MoviesSyncApiClient
import tv.trakt.trakt.common.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.common.core.sync.data.remote.shows.ShowsSyncApiClient
import tv.trakt.trakt.common.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

val syncRemoteModule = module {
    singleOf(::ShowsSyncApiClient) { bind<ShowsSyncRemoteDataSource>() }
    singleOf(::MoviesSyncApiClient) { bind<MoviesSyncRemoteDataSource>() }
    singleOf(::EpisodesSyncApiClient) { bind<EpisodesSyncRemoteDataSource>() }
}
