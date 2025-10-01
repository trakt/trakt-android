package tv.trakt.trakt.core.lists.sections.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

internal class GetPersonalListItemsUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: ListsPersonalItemsLocalDataSource,
) {
    suspend fun getLocalItems(listId: TraktId): ImmutableList<PersonalListItem> {
        return localSource.getItems(listId)
            .toImmutableList()
    }

    suspend fun getItems(
        listId: TraktId,
        limit: Int,
    ): ImmutableList<PersonalListItem> {
        return remoteSource.getPersonalListItems(
            listId = listId,
            limit = limit,
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
        }
            .toImmutableList()
            .also {
                localSource.setItems(
                    listId = listId,
                    items = it,
                )
            }
    }
}
