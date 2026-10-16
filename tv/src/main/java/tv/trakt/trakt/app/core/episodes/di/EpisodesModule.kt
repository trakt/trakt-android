package tv.trakt.trakt.app.core.episodes.di

import org.koin.dsl.module
import tv.trakt.trakt.common.core.media.episodes.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.media.episodes.local.EpisodeStorage

internal val episodesDataModule = module {
    single<EpisodeLocalDataSource> {
        EpisodeStorage()
    }
}
