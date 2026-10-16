package tv.trakt.trakt.common.core.episodes.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.episodes.data.remote.EpisodesApiClient
import tv.trakt.trakt.common.core.episodes.data.remote.EpisodesRemoteDataSource

val episodesRemoteModule = module {
    singleOf(::EpisodesApiClient) { bind<EpisodesRemoteDataSource>() }
}
