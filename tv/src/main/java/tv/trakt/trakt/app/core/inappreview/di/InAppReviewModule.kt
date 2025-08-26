package tv.trakt.trakt.app.core.inappreview.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.app.core.inappreview.usecases.RequestAppReviewUseCase
import tv.trakt.trakt.app.core.main.di.MAIN_PREFERENCES

internal val inAppReviewModule = module {
    factory {
        RequestAppReviewUseCase(
            mainDataStore = get(named(MAIN_PREFERENCES)),
        )
    }
}
