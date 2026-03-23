package tv.trakt.trakt.app.core.episodes.di

import org.koin.dsl.module
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesApiClient
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeStorage

internal val episodesDataModule = module {
    single<EpisodesRemoteDataSource> {
        EpisodesApiClient(
            showsApi = get(),
            usersApi = get(),
        )
    }

    single<EpisodeLocalDataSource> {
        EpisodeStorage()
    }
}
