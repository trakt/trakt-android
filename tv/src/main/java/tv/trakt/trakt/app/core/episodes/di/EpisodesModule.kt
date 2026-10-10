package tv.trakt.trakt.app.core.episodes.di

import org.koin.dsl.module
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesApiClient
import tv.trakt.trakt.app.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.common.core.media.episodes.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.media.episodes.local.EpisodeStorage

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
