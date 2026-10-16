package tv.trakt.trakt.core.sync.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieFavoritesUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowFavoritesUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase

internal val syncModule = module {
    singleOf(::CollectionStateProvider)

    factoryOf(::UpdateMovieHistoryUseCase)

    factoryOf(::UpdateMovieWatchlistUseCase)

    factoryOf(::UpdateMovieFavoritesUseCase)

    factoryOf(::UpdateShowWatchlistUseCase)

    factoryOf(::UpdateShowFavoritesUseCase)

    factoryOf(::UpdateEpisodeHistoryUseCase)

    factoryOf(::UpdateShowHistoryUseCase)
}
