package tv.trakt.trakt.core.summary.episodes.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeStorage
import tv.trakt.trakt.core.summary.episodes.EpisodeDetailsViewModel
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeDetailsUseCase

internal val episodeDetailsDataModule = module {
    single<EpisodeLocalDataSource> {
        EpisodeStorage()
    }
}

internal val episodeDetailsModule = module {
    factory {
        GetEpisodeDetailsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    viewModel {
        EpisodeDetailsViewModel(
            savedStateHandle = get(),
            getShowDetailsUseCase = get(),
            getEpisodeDetailsUseCase = get(),
            sessionManager = get(),
        )
    }
}
