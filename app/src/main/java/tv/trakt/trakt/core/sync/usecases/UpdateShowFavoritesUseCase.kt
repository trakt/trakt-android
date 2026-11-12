package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.data.local.UserFavoritesLocalSource
import tv.trakt.trakt.common.data.remote.ShowsSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

internal class UpdateShowFavoritesUseCase(
    private val remoteSource: ShowsSyncRemoteSource,
    private val userFavoritesLocalSource: UserFavoritesLocalSource,
    private val dataChanges: DataChanges,
) {
    suspend fun addToFavorites(show: Show) {
        remoteSource.addToFavorites(
            showId = show.ids.trakt,
        )

        userFavoritesLocalSource.addShows(
            shows = listOf(
                FavoriteItem.ShowItem(
                    rank = 0,
                    show = show,
                    listedAt = nowUtcInstant(),
                ),
            ),
        )
        dataChanges.notify(Kind.Favorites)
    }

    suspend fun removeFromFavorites(showId: TraktId) {
        remoteSource.removeFromFavorites(
            showId = showId,
        )

        userFavoritesLocalSource.removeShows(setOf(showId))
        dataChanges.notify(Kind.Favorites)
    }
}
