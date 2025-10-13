package tv.trakt.trakt.core.lists.features.details.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.model.PersonalListItem

internal class GetListItemsUseCase(
    private val remoteSource: ListsRemoteDataSource,
) {
    suspend fun getItems(
        listId: TraktId,
        type: MediaType?,
    ): ImmutableList<PersonalListItem> {
        if (type == MediaType.MOVIE) {
            return remoteSource.getMovieListItems(
                listId = listId,
                limit = "all",
                extended = "full,cloud9,colors",
            ).asyncMap {
                PersonalListItem.MovieItem(
                    movie = Movie.fromDto(it.movie),
                    listedAt = it.listedAt.toInstant(),
                )
            }.toImmutableList()
        }

        if (type == MediaType.SHOW) {
            return remoteSource.getShowListItems(
                listId = listId,
                limit = "all",
                extended = "full,cloud9,colors",
            ).asyncMap {
                PersonalListItem.ShowItem(
                    show = Show.fromDto(it.show),
                    listedAt = it.listedAt.toInstant(),
                )
            }.toImmutableList()
        }

        return remoteSource.getAllListItems(
            listId = listId,
            limit = "all",
            extended = "full,cloud9,colors",
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()

            when {
                it.movie != null -> {
                    PersonalListItem.MovieItem(
                        movie = Movie.fromDto(it.movie!!),
                        listedAt = listedAt,
                    )
                }
                it.show != null -> {
                    PersonalListItem.ShowItem(
                        show = Show.fromDto(it.show!!),
                        listedAt = listedAt,
                    )
                }
                else -> {
                    throw IllegalStateException("Watchlist item unknown type!")
                }
            }
        }.toImmutableList()
    }
}
