package tv.trakt.trakt.core.summary.social.di

import kotlinx.collections.immutable.ImmutableList
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.summary.social.MediaSocialActivityViewModel
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity
import tv.trakt.trakt.core.summary.social.recommendedby.RecommendedByViewModel
import tv.trakt.trakt.core.summary.social.recommendedby.usecases.GetRecommendedByUseCase

internal val mediaSocialActivityModule = module {
    factoryOf(::GetRecommendedByUseCase)

    viewModel { (activity: ImmutableList<MediaSocialActivity>) ->
        MediaSocialActivityViewModel(
            activity = activity,
        )
    }

    viewModel { (path: String) ->
        RecommendedByViewModel(
            path = path,
            getRecommendedByUseCase = get(),
            shareArrivalEvents = get(),
        )
    }
}
