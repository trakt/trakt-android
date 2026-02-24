package tv.trakt.trakt.core.lists.sections.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaType.EPISODE
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SEASON
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.main.model.MediaMode.MEDIA
import tv.trakt.trakt.core.main.model.MediaMode.MOVIES
import tv.trakt.trakt.core.main.model.MediaMode.SHOWS

internal class GetPersonalListItemsUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: ListsPersonalItemsLocalDataSource,
) {
    suspend fun getItems(
        listId: TraktId,
        limit: Int,
        filter: MediaMode,
        sorting: Sorting,
    ): ImmutableList<CustomListItem> {
        return remoteSource.getPersonalListItems(
            listId = listId,
            limit = limit,
            page = 1,
            extended = "full,cloud9,colors",
            sorting = sorting,
        )
            .asyncMap(::mapListItem)
            .distinctBy { it.key }
            .also {
                localSource.setItems(
                    listId = listId,
                    items = it,
                )
            }.filter {
                when (filter) {
                    MEDIA -> true
                    SHOWS -> it is CustomListItem.ShowItem
                    MOVIES -> it is CustomListItem.MovieItem
                }
            }.toImmutableList()
    }

    suspend fun getLocalItems(
        listId: TraktId,
        filter: MediaMode,
    ): ImmutableList<CustomListItem> {
        return localSource.getItems(listId)
            .filter {
                when (filter) {
                    MEDIA -> true
                    SHOWS -> it is CustomListItem.ShowItem
                    MOVIES -> it is CustomListItem.MovieItem
                }
            }
            .distinctBy { it.key }
            .toImmutableList()
    }

    suspend fun getRemoteItems(
        listId: TraktId,
        page: Int,
        limit: Int,
        type: MediaMode,
        sorting: Sorting,
    ): ImmutableList<CustomListItem> {
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
    ): ImmutableList<CustomListItem> {
        return remoteSource.getPersonalListMovieItems(
            listId = listId,
            limit = limit,
            page = page,
            extended = "full,cloud9,colors",
            sorting = sorting,
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()
            CustomListItem.MovieItem(
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
    ): ImmutableList<CustomListItem> {
        return remoteSource.getPersonalListShowItems(
            listId = listId,
            limit = limit,
            page = page,
            extended = "full,cloud9,colors",
            sorting = sorting,
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()
            CustomListItem.ShowItem(
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
    ): ImmutableList<CustomListItem> {
        return remoteSource.getPersonalListItems(
            listId = listId,
            limit = limit,
            page = page,
            extended = "full,cloud9,colors",
            sorting = sorting,
        )
            .asyncMap(::mapListItem)
            .toImmutableList()
    }

    private fun mapListItem(dto: ListItemDto): CustomListItem {
        return when (dto.type.value) {
            SHOW.value -> CustomListItem.ShowItem(
                rank = dto.rank,
                show = Show.fromDto(dto.show!!),
                listedAt = dto.listedAt.toInstant(),
            )
            MOVIE.value -> CustomListItem.MovieItem(
                rank = dto.rank,
                movie = Movie.fromDto(dto.movie!!),
                listedAt = dto.listedAt.toInstant(),
            )
            SEASON.value -> CustomListItem.SeasonItem(
                rank = dto.rank,
                season = Season.fromDto(dto.season!!),
                show = Show.fromDto(dto.show!!),
                listedAt = dto.listedAt.toInstant(),
            )
            EPISODE.value -> CustomListItem.EpisodeItem(
                rank = dto.rank,
                episode = Episode.fromDto(dto.episode!!),
                show = Show.fromDto(dto.show!!),
                listedAt = dto.listedAt.toInstant(),
            )
            else -> throw IllegalArgumentException("Unknown media type: ${dto.type}")
        }
    }
}
