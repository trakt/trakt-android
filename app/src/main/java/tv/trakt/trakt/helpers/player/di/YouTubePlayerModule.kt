package tv.trakt.trakt.helpers.player.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.helpers.player.YouTubePlayerViewModel

internal val youTubePlayerModule = module {
    viewModel {
        YouTubePlayerViewModel(
            savedStateHandle = get(),
        )
    }
}
