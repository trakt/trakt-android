package tv.trakt.trakt.core.favorites.di

import org.koin.dsl.module
import tv.trakt.trakt.core.favorites.FavoritesUpdates
import tv.trakt.trakt.core.favorites.FavoritesUpdatesStorage

internal val favoritesDataModule = module {
    single<FavoritesUpdates> {
        FavoritesUpdatesStorage()
    }
}
