package tv.trakt.trakt.core.summary.soundtrack.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.soundtrack.SoundtrackViewModel
import tv.trakt.trakt.core.summary.soundtrack.all.AllSoundtrackViewModel
import tv.trakt.trakt.core.summary.soundtrack.usecases.GetSoundtrackUseCase

internal val soundtrackModule = module {
    factoryOf(::GetSoundtrackUseCase)

    viewModelOf(::AllSoundtrackViewModel)

    viewModel { (mediaType: MediaType, mediaId: TraktId) ->
        SoundtrackViewModel(
            mediaType = mediaType,
            mediaId = mediaId,
            getSoundtrackUseCase = get(),
            collapsingManager = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }
}
