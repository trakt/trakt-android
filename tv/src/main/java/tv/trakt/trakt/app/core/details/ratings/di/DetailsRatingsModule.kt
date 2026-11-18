package tv.trakt.trakt.app.core.details.ratings.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tv.trakt.trakt.app.core.details.ratings.usecases.ChangeFavoriteUseCase
import tv.trakt.trakt.app.core.details.ratings.usecases.ChangeRatingUseCase
import tv.trakt.trakt.app.core.details.ratings.usecases.GetUserRatingUseCase
import tv.trakt.trakt.app.core.details.ratings.usecases.IsFavoriteUseCase
import tv.trakt.trakt.app.core.details.ratings.usecases.IsWatchedUseCase
import tv.trakt.trakt.common.core.favorites.usecases.UpdateMovieFavoritesUseCase
import tv.trakt.trakt.common.core.favorites.usecases.UpdateShowFavoritesUseCase
import tv.trakt.trakt.common.core.ratings.usecases.DeleteRatingUseCase
import tv.trakt.trakt.common.core.ratings.usecases.PostRatingUseCase
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserFavoritesUseCase

internal val detailsRatingsModule = module {
    factoryOf(::PostRatingUseCase)
    factoryOf(::DeleteRatingUseCase)
    factoryOf(::GetUserRatingUseCase)
    factoryOf(::ChangeRatingUseCase)
    factoryOf(::IsWatchedUseCase)
    factoryOf(::UpdateMovieFavoritesUseCase)
    factoryOf(::UpdateShowFavoritesUseCase)
    factoryOf(::LoadUserFavoritesUseCase)
    factoryOf(::IsFavoriteUseCase)
    factoryOf(::ChangeFavoriteUseCase)
}
