package tv.trakt.trakt.core.summary.livingposter.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.livingposter.LivingPosterViewModel
import tv.trakt.trakt.core.summary.livingposter.usecases.GetLivingPosterUseCase

internal val livingPosterModule = module {
    factoryOf(::GetLivingPosterUseCase)

    viewModel { (mediaType: MediaType, mediaId: TraktId) ->
        LivingPosterViewModel(
            mediaType = mediaType,
            mediaId = mediaId,
            getLivingPosterUseCase = get(),
            featureFlagsUseCase = get(),
        )
    }
}
