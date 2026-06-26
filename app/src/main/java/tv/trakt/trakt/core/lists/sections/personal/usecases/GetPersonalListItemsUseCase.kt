package tv.trakt.trakt.core.lists.sections.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.personallists.UserPersonalListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaMode.MEDIA
import tv.trakt.trakt.common.model.MediaMode.MOVIES
import tv.trakt.trakt.common.model.MediaMode.SHOWS
import tv.trakt.trakt.common.model.MediaType.EPISODE
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SEASON
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase

internal class GetPersonalListItemsUseCase(
    private val remoteSource: UserPersonalListsRemoteDataSource,
    private val localSource: ListsPersonalItemsLocalDataSource,
    private val loadUserRatingsUseCase: LoadUserRatingsUseCase,
) {
    suspend fun getItems(
        listId: TraktId,
        limit: Int,
        filter: GlobalFilter,
        sorting: Sorting,
    ): ImmutableList<CustomListItem> {
        return remoteSource.getPersonalListItems(
            listId = listId,
            limit = limit,
            page = 1,
            extended = "full,cloud9,colors",
            sorting = sorting,
            filters = filter,
        )
            .asyncMap(::mapListItem)
            .distinctBy { it.key }
            .also {
                localSource.setItems(
                    listId = listId,
                    items = it,
                )
            }.filter {
                when (filter.mode) {
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
        filter: GlobalFilter,
        sorting: Sorting,
    ): ImmutableList<CustomListItem> {
        val (showsRatings, moviesRatings, episodesRatings) =
            loadUserRatingsUseCase.loadAllIfNeeded()

        return when (filter.mode) {
            MEDIA -> getRemoteAllItems(
                listId = listId,
                page = page,
                limit = limit,
                filter = filter,
                sorting = sorting,
                showsRatings = showsRatings,
                moviesRatings = moviesRatings,
                episodesRatings = episodesRatings,
            )

            SHOWS -> getRemoteShowItems(
                listId = listId,
                page = page,
                limit = limit,
                filter = filter,
                sorting = sorting,
                showsRatings = showsRatings,
            )

            MOVIES -> getRemoteMovieItems(
                listId = listId,
                page = page,
                limit = limit,
                filter = filter,
                sorting = sorting,
                moviesRatings = moviesRatings,
            )
        }
    }

    private suspend fun getRemoteMovieItems(
        listId: TraktId,
        page: Int,
        limit: Int,
        filter: GlobalFilter,
        sorting: Sorting,
        moviesRatings: ImmutableMap<TraktId, UserRating>,
    ): ImmutableList<CustomListItem> {
        return remoteSource.getPersonalListMovieItems(
            listId = listId,
            limit = limit,
            page = page,
            extended = "full,cloud9,colors",
            sorting = sorting,
            filters = filter,
        ).asyncMap {
            val movie = Movie.fromDto(it.movie)
            val listedAt = it.listedAt.toInstant()
            CustomListItem.MovieItem(
                itemId = it.id,
                rank = it.rank,
                movie = movie,
                listedAt = listedAt,
                userRating = moviesRatings[movie.ids.trakt],
            )
        }.toImmutableList()
    }

    private suspend fun getRemoteShowItems(
        listId: TraktId,
        page: Int,
        limit: Int,
        filter: GlobalFilter,
        sorting: Sorting,
        showsRatings: ImmutableMap<TraktId, UserRating>,
    ): ImmutableList<CustomListItem> {
        return remoteSource.getPersonalListShowItems(
            listId = listId,
            limit = limit,
            page = page,
            extended = "full,cloud9,colors",
            sorting = sorting,
            filters = filter,
        ).asyncMap {
            val show = Show.fromDto(it.show)
            val listedAt = it.listedAt.toInstant()
            CustomListItem.ShowItem(
                itemId = it.id,
                rank = it.rank,
                show = show,
                listedAt = listedAt,
                userRating = showsRatings[show.ids.trakt],
            )
        }.toImmutableList()
    }

    private suspend fun getRemoteAllItems(
        listId: TraktId,
        page: Int,
        limit: Int,
        filter: GlobalFilter,
        sorting: Sorting,
        showsRatings: ImmutableMap<TraktId, UserRating>,
        moviesRatings: ImmutableMap<TraktId, UserRating>,
        episodesRatings: ImmutableMap<TraktId, UserRating>,
    ): ImmutableList<CustomListItem> {
        return remoteSource.getPersonalListItems(
            listId = listId,
            limit = limit,
            page = page,
            extended = "full,cloud9,colors",
            sorting = sorting,
            filters = filter,
        )
            .asyncMap {
                mapListItem(
                    dto = it,
                    showsRatings = showsRatings,
                    moviesRatings = moviesRatings,
                    episodesRatings = episodesRatings,
                )
            }
            .toImmutableList()
    }

    private fun mapListItem(
        dto: ListItemDto,
        showsRatings: ImmutableMap<TraktId, UserRating>? = null,
        moviesRatings: ImmutableMap<TraktId, UserRating>? = null,
        episodesRatings: ImmutableMap<TraktId, UserRating>? = null,
    ): CustomListItem {
        return when (dto.type.value) {
            SHOW.value -> {
                val show = Show.fromDto(dto.show!!)
                CustomListItem.ShowItem(
                    itemId = dto.id,
                    rank = dto.rank,
                    show = show,
                    listedAt = dto.listedAt.toInstant(),
                    userRating = showsRatings?.get(show.ids.trakt),
                )
            }
            MOVIE.value -> {
                val movie = Movie.fromDto(dto.movie!!)
                CustomListItem.MovieItem(
                    itemId = dto.id,
                    rank = dto.rank,
                    movie = movie,
                    listedAt = dto.listedAt.toInstant(),
                    userRating = moviesRatings?.get(movie.ids.trakt),
                )
            }
            SEASON.value -> {
                CustomListItem.SeasonItem(
                    itemId = dto.id,
                    rank = dto.rank,
                    season = Season.fromDto(dto.season!!),
                    show = Show.fromDto(dto.show!!),
                    listedAt = dto.listedAt.toInstant(),
                    userRating = null,
                )
            }
            EPISODE.value -> {
                val show = Show.fromDto(dto.show!!)
                val episode = Episode.fromDto(dto.episode!!)
                CustomListItem.EpisodeItem(
                    itemId = dto.id,
                    rank = dto.rank,
                    episode = episode,
                    show = show,
                    listedAt = dto.listedAt.toInstant(),
                    userRating = episodesRatings?.get(episode.ids.trakt),
                )
            }
            else -> {
                throw IllegalArgumentException("Unknown media type: ${dto.type}")
            }
        }
    }
}
