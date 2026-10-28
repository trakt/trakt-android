package tv.trakt.trakt.core.inreview.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.inreview.InReviewViewModel

internal val inReviewModule = module {
    viewModelOf(::InReviewViewModel)
}
