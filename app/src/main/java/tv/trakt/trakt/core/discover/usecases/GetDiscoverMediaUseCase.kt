package tv.trakt.trakt.core.discover.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.media.data.remote.MediaRemoteDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.networking.AnticipatedMediaDto
import tv.trakt.trakt.common.networking.PopularMediaDto
import tv.trakt.trakt.common.networking.TrendingMediaDto
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.data.local.media.DiscoverMediaLocalDataSource
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.model.DiscoverSection.Anticipated
import tv.trakt.trakt.core.discover.model.DiscoverSection.Popular
import tv.trakt.trakt.core.discover.model.DiscoverSection.Recommended
import tv.trakt.trakt.core.discover.model.DiscoverSection.Trending
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedItem
import tv.trakt.trakt.core.home.sections.recommended.usecase.media.GetRecommendedMediaUseCase

/**
 * Loads interleaved shows and movies from the media endpoints for Media mode.
 */
internal class GetDiscoverMediaUseCase(
    private val remoteSource: MediaRemoteDataSource,
    private val localMediaSource: DiscoverMediaLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
    private val localMovieSource: MovieLocalDataSource,
    private val getRecommendedMediaUseCase: GetRecommendedMediaUseCase,
) {
    suspend fun getLocalMedia(section: DiscoverSection): ImmutableList<DiscoverItem> {
        if (section == Recommended) {
            return getRecommendedMediaUseCase.getLocalMedia().toDiscoverItems()
        }

        return localMediaSource.getItems(section)
            .toImmutableList()
            .also { upsertMedia(it) }
    }

    suspend fun getMedia(
        section: DiscoverSection,
        page: Int = 1,
        limit: Int = DEFAULT_SECTION_LIMIT,
        skipLocal: Boolean = false,
        filters: GlobalFilter,
    ): ImmutableList<DiscoverItem> {
        if (section == Recommended) {
            return getRecommendedMedia(page = page, limit = limit, skipLocal = skipLocal, filters = filters)
        }
        val items = when (section) {
            Trending -> remoteSource.getTrending(page = page, limit = limit, filters = filters)
                .mapNotNull(::toDiscoverItem)

            Popular -> remoteSource.getPopular(page = page, limit = limit, filters = filters)
                .mapIndexed { index, dto -> toDiscoverItem(dto = dto, rank = index + 1) }

            Anticipated -> remoteSource.getAnticipated(page = page, limit = limit, filters = filters)
                .mapNotNull(::toDiscoverItem)

            Recommended -> error("Recommended is loaded through GetRecommendedMediaUseCase")
        }.toImmutableList()

        if (!skipLocal) {
            localMediaSource.setItems(
                section = section,
                items = items.take(DEFAULT_SECTION_LIMIT),
            )
        }
        upsertMedia(items)

        return items
    }

    private suspend fun getRecommendedMedia(
        page: Int,
        limit: Int,
        skipLocal: Boolean,
        filters: GlobalFilter,
    ): ImmutableList<DiscoverItem> {
        // Recommendations are not paginated.
        if (page > 1) {
            return persistentListOf()
        }

        return getRecommendedMediaUseCase.getMedia(
            limit = limit,
            skipLocal = skipLocal,
            filters = filters,
        ).toDiscoverItems()
    }

    private suspend fun upsertMedia(items: List<DiscoverItem>) {
        localShowSource.upsertShows(
            items.filterIsInstance<DiscoverItem.ShowItem>().map { it.show },
        )
        localMovieSource.upsertMovies(
            items.filterIsInstance<DiscoverItem.MovieItem>().map { it.movie },
        )
    }
}

private fun List<RecommendedItem>.toDiscoverItems(): ImmutableList<DiscoverItem> {
    return map {
        when (it) {
            is RecommendedItem.ShowItem -> DiscoverItem.ShowItem(show = it.show, sources = it.sources)
            is RecommendedItem.MovieItem -> DiscoverItem.MovieItem(movie = it.movie, sources = it.sources)
        }
    }.toImmutableList()
}

private fun toDiscoverItem(dto: TrendingMediaDto): DiscoverItem? {
    return dto.show?.let { DiscoverItem.ShowItem(show = Show.fromDto(it), count = dto.watchers) }
        ?: dto.movie?.let { DiscoverItem.MovieItem(movie = Movie.fromDto(it), count = dto.watchers) }
}

private fun toDiscoverItem(dto: AnticipatedMediaDto): DiscoverItem? {
    return dto.show?.let { DiscoverItem.ShowItem(show = Show.fromDto(it), count = dto.listCount) }
        ?: dto.movie?.let { DiscoverItem.MovieItem(movie = Movie.fromDto(it), count = dto.listCount) }
}

/**
 * Popular items are bare show or movie objects; only shows carry aired episodes.
 */
private fun toDiscoverItem(
    dto: PopularMediaDto,
    rank: Int,
): DiscoverItem {
    return when {
        dto.airedEpisodes != null -> DiscoverItem.ShowItem(show = Show.fromDto(dto), count = rank)
        else -> DiscoverItem.MovieItem(movie = Movie.fromDto(dto), count = rank)
    }
}
