package tv.trakt.trakt.common.core.episodes.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.episodes.data.remote.EpisodesRemoteSource

val episodesRemoteModule = module {
    singleOf(::EpisodesRemoteSource)
}
