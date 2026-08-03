package tv.trakt.trakt.app.core.lists.details.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.lists.details.personal.model.PersonalListItem
import tv.trakt.trakt.app.core.lists.filters.TvListPage
import tv.trakt.trakt.app.core.lists.filters.TvListRequest
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.user.data.remote.personallists.UserPersonalListsRemoteDataSource
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.ListItemDto

internal class GetPersonalListItemsUseCase(
    private val remoteSource: UserPersonalListsRemoteDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
) {
    suspend fun getListItems(
        listId: TraktId,
        request: TvListRequest,
    ): TvListPage<PersonalListItem> {
        return when (request.filter.mode) {
            MediaMode.Media -> getMediaItems(listId, request)
            MediaMode.Shows -> getShowItems(listId, request)
            MediaMode.Movies -> getMovieItems(listId, request)
        }
    }

    private suspend fun getMediaItems(
        listId: TraktId,
        request: TvListRequest,
    ): TvListPage<PersonalListItem> {
        var page = request.page
        var hasMore = true
        var items: ImmutableList<PersonalListItem> = persistentListOf()

        while (items.isEmpty() && hasMore) {
            val response = remoteSource.getPersonalListItems(
                listId = listId,
                limit = request.limit,
                page = page,
                extended = EXTENDED,
                sorting = request.sorting,
                filters = request.filter,
            )

            items = response
                .mapNotNull(::mapSupportedItem)
                .toImmutableList()
            page += 1
            hasMore = response.size >= request.limit
        }

        persist(items)

        return TvListPage(
            items = items,
            nextPage = page,
            hasMore = hasMore,
        )
    }

    private suspend fun getShowItems(
        listId: TraktId,
        request: TvListRequest,
    ): TvListPage<PersonalListItem> {
        val response = remoteSource.getPersonalListShowItems(
            listId = listId,
            limit = request.limit,
            page = request.page,
            extended = EXTENDED,
            sorting = request.sorting,
            filters = request.filter,
        )
        val items = response.map {
            PersonalListItem(
                type = MediaType.Show.value,
                rank = it.rank,
                show = Show.fromDto(it.show),
            )
        }.toImmutableList()

        persist(items)

        return TvListPage(
            items = items,
            nextPage = request.page + 1,
            hasMore = response.size >= request.limit,
        )
    }

    private suspend fun getMovieItems(
        listId: TraktId,
        request: TvListRequest,
    ): TvListPage<PersonalListItem> {
        val response = remoteSource.getPersonalListMovieItems(
            listId = listId,
            limit = request.limit,
            page = request.page,
            extended = EXTENDED,
            sorting = request.sorting,
            filters = request.filter,
        )
        val items = response.map {
            PersonalListItem(
                type = MediaType.Movie.value,
                rank = it.rank,
                movie = Movie.fromDto(it.movie),
            )
        }.toImmutableList()

        persist(items)

        return TvListPage(
            items = items,
            nextPage = request.page + 1,
            hasMore = response.size >= request.limit,
        )
    }

    private fun mapSupportedItem(dto: ListItemDto): PersonalListItem? {
        return when (dto.type.value) {
            MediaType.Show.value -> dto.show?.let {
                PersonalListItem(
                    type = MediaType.Show.value,
                    rank = dto.rank,
                    show = Show.fromDto(it),
                )
            }

            MediaType.Movie.value -> dto.movie?.let {
                PersonalListItem(
                    type = MediaType.Movie.value,
                    rank = dto.rank,
                    movie = Movie.fromDto(it),
                )
            }

            MediaType.Season.value,
            MediaType.Episode.value,
            -> null

            else -> null
        }
    }

    private suspend fun persist(items: ImmutableList<PersonalListItem>) {
        localShowSource.upsertShows(items.mapNotNull { it.show })
        localMovieSource.upsertMovies(items.mapNotNull { it.movie })
    }

    private companion object {
        const val EXTENDED = "full,cloud9,streaming_ids"
    }
}
