package tv.trakt.trakt.core.lists.sections.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.main.model.MediaMode.MEDIA
import tv.trakt.trakt.core.main.model.MediaMode.MOVIES
import tv.trakt.trakt.core.main.model.MediaMode.SHOWS
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

internal class GetPersonalListItemsUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: ListsPersonalItemsLocalDataSource,
) {
    suspend fun getItems(
        listId: TraktId,
        limit: Int,
        filter: MediaMode,
        sorting: Sorting,
    ): ImmutableList<PersonalListItem> {
        return remoteSource.getPersonalListItems(
            listId = listId,
            limit = limit,
            page = 1,
            extended = "full,cloud9,colors",
            sorting = sorting,
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()
            when {
                it.movie != null -> {
                    PersonalListItem.MovieItem(
                        rank = it.rank,
                        movie = Movie.fromDto(it.movie!!),
                        listedAt = listedAt,
                    )
                }

                it.show != null -> {
                    PersonalListItem.ShowItem(
                        rank = it.rank,
                        show = Show.fromDto(it.show!!),
                        listedAt = listedAt,
                    )
                }

                else -> {
                    throw IllegalStateException("Watchlist item unknown type!")
                }
            }
        }.also {
            localSource.setItems(
                listId = listId,
                items = it,
            )
        }.filter {
            when (filter) {
                MEDIA -> true
                SHOWS -> it is PersonalListItem.ShowItem
                MOVIES -> it is PersonalListItem.MovieItem
            }
        }.toImmutableList()
    }

    suspend fun getLocalItems(
        listId: TraktId,
        filter: MediaMode,
    ): ImmutableList<PersonalListItem> {
        return localSource.getItems(listId)
            .filter {
                when (filter) {
                    MEDIA -> true
                    SHOWS -> it is PersonalListItem.ShowItem
                    MOVIES -> it is PersonalListItem.MovieItem
                }
            }.toImmutableList()
    }

    suspend fun getRemoteItems(
        listId: TraktId,
        page: Int,
        limit: Int,
        type: MediaMode,
        sorting: Sorting,
    ): ImmutableList<PersonalListItem> {
        return when (type) {
            MEDIA -> getRemoteAllItems(
                listId = listId,
                page = page,
                limit = limit,
                sorting = sorting,
            )

            SHOWS -> getRemoteShowItems(
                listId = listId,
                page = page,
                limit = limit,
                sorting = sorting,
            )

            MOVIES -> getRemoteMovieItems(
                listId = listId,
                page = page,
                limit = limit,
                sorting = sorting,
            )
        }
    }

    private suspend fun getRemoteMovieItems(
        listId: TraktId,
        page: Int,
        limit: Int,
        sorting: Sorting,
    ): ImmutableList<PersonalListItem> {
        return remoteSource.getPersonalListMovieItems(
            listId = listId,
            limit = limit,
            page = page,
            extended = "full,cloud9,colors",
            sorting = sorting,
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()
            PersonalListItem.MovieItem(
                rank = it.rank,
                movie = Movie.fromDto(it.movie),
                listedAt = listedAt,
            )
        }.toImmutableList()
    }

    private suspend fun getRemoteShowItems(
        listId: TraktId,
        page: Int,
        limit: Int,
        sorting: Sorting,
    ): ImmutableList<PersonalListItem> {
        return remoteSource.getPersonalListShowItems(
            listId = listId,
            limit = limit,
            page = page,
            extended = "full,cloud9,colors",
            sorting = sorting,
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()
            PersonalListItem.ShowItem(
                rank = it.rank,
                show = Show.fromDto(it.show),
                listedAt = listedAt,
            )
        }.toImmutableList()
    }

    private suspend fun getRemoteAllItems(
        listId: TraktId,
        page: Int,
        limit: Int,
        sorting: Sorting,
    ): ImmutableList<PersonalListItem> {
        return remoteSource.getPersonalListItems(
            listId = listId,
            limit = limit,
            page = page,
            extended = "full,cloud9,colors",
            sorting = sorting,
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()
            when {
                it.movie != null -> {
                    PersonalListItem.MovieItem(
                        rank = it.rank,
                        movie = Movie.fromDto(it.movie!!),
                        listedAt = listedAt,
                    )
                }

                it.show != null -> {
                    PersonalListItem.ShowItem(
                        rank = it.rank,
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
