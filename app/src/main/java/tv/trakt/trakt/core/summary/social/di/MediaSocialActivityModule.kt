package tv.trakt.trakt.core.summary.social.di

import kotlinx.collections.immutable.ImmutableList
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.summary.social.MediaSocialActivityViewModel
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity

internal val mediaSocialActivityModule = module {
    viewModel { (activity: ImmutableList<MediaSocialActivity>) ->
        MediaSocialActivityViewModel(
            activity = activity,
        )
    }
}
