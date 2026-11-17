package tv.trakt.trakt.app.core.details.ratings.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tv.trakt.trakt.app.core.details.ratings.usecases.ChangeRatingUseCase
import tv.trakt.trakt.app.core.details.ratings.usecases.GetUserRatingUseCase
import tv.trakt.trakt.app.core.details.ratings.usecases.IsWatchedUseCase
import tv.trakt.trakt.common.core.ratings.usecases.DeleteRatingUseCase
import tv.trakt.trakt.common.core.ratings.usecases.PostRatingUseCase

internal val detailsRatingsModule = module {
    factoryOf(::PostRatingUseCase)
    factoryOf(::DeleteRatingUseCase)
    factoryOf(::GetUserRatingUseCase)
    factoryOf(::ChangeRatingUseCase)
    factoryOf(::IsWatchedUseCase)
}
