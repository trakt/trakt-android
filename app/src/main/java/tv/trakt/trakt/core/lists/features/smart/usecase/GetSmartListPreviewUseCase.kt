package tv.trakt.trakt.core.lists.features.smart.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListSource.Anticipated
import tv.trakt.trakt.common.model.lists.SmartListSource.Popular
import tv.trakt.trakt.common.model.lists.SmartListSource.Recommendations
import tv.trakt.trakt.common.model.lists.SmartListSource.Trending
import tv.trakt.trakt.common.networking.MovieCalendarDto
import tv.trakt.trakt.common.networking.RecommendedMovieDto
import tv.trakt.trakt.common.networking.RecommendedShowDto
import tv.trakt.trakt.common.networking.ShowCalendarsDto
import tv.trakt.trakt.core.lists.model.SmartListItem
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.data.remote.model.AnticipatedMovieDto
import tv.trakt.trakt.core.movies.data.remote.model.TrendingMovieDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.data.remote.model.AnticipatedShowDto
import tv.trakt.trakt.core.shows.data.remote.model.TrendingShowDto

private const val MAX_PREVIEW_ITEMS = 100

internal class GetSmartListPreviewUseCase(
    private val remoteShowsSource: ShowsRemoteDataSource,
    private val remoteMoviesSource: MoviesRemoteDataSource,
) {
    suspend fun getPreviewItems(filters: SmartListFilters): ImmutableList<SmartListItem> {
        return when (filters.media) {
            Shows -> getShowsPreview(filters)
            Movies -> getMoviesPreview(filters)
            else -> throw IllegalArgumentException("Unsupported media mode: ${filters.media}")
        }
    }

    private suspend fun getShowsPreview(filters: SmartListFilters): ImmutableList<SmartListItem> {
        return when (filters.source) {
            Trending -> remoteShowsSource.getTrending(
                limit = MAX_PREVIEW_ITEMS,
                filters = mapFilters(filters),
            )
            Popular -> remoteShowsSource.getPopular(
                limit = MAX_PREVIEW_ITEMS,
                filters = mapFilters(filters),
            )
            Anticipated -> remoteShowsSource.getAnticipated(
                limit = MAX_PREVIEW_ITEMS,
                filters = mapFilters(filters),
            )
            Recommendations -> remoteShowsSource.getRecommended(
                limit = MAX_PREVIEW_ITEMS,
                filters = mapFilters(filters),
            )
            else -> throw IllegalArgumentException("Unsupported source for shows: ${filters.source}")
        }.asyncMap { dto ->
            SmartListItem.ShowItem(
                show = when (dto) {
                    is TrendingShowDto -> Show.fromDto(dto.show)
                    is ShowCalendarsDto -> Show.fromDto(dto)
                    is AnticipatedShowDto -> Show.fromDto(dto.show)
                    is RecommendedShowDto -> Show.fromDto(dto)
                    else -> throw IllegalArgumentException("Unsupported DTO type: ${dto::class.simpleName}")
                },
            )
        }.toImmutableList()
    }

    private suspend fun getMoviesPreview(filters: SmartListFilters): ImmutableList<SmartListItem> {
        return when (filters.source) {
            Trending -> remoteMoviesSource.getTrending(
                limit = MAX_PREVIEW_ITEMS,
                filters = mapFilters(filters),
            )
            Popular -> remoteMoviesSource.getPopular(
                limit = MAX_PREVIEW_ITEMS,
                filters = mapFilters(filters),
            )
            Anticipated -> remoteMoviesSource.getAnticipated(
                limit = MAX_PREVIEW_ITEMS,
                filters = mapFilters(filters),
            )
            Recommendations -> remoteMoviesSource.getRecommended(
                limit = MAX_PREVIEW_ITEMS,
                filters = mapFilters(filters),
            )
            else -> throw IllegalArgumentException("Unsupported source for movies: ${filters.source}")
        }.asyncMap { dto ->
            SmartListItem.MovieItem(
                movie = when (dto) {
                    is TrendingMovieDto -> Movie.fromDto(dto.movie)
                    is MovieCalendarDto -> Movie.fromDto(dto)
                    is AnticipatedMovieDto -> Movie.fromDto(dto.movie)
                    is RecommendedMovieDto -> Movie.fromDto(dto)
                    else -> throw IllegalArgumentException("Unsupported DTO type: ${dto::class.simpleName}")
                },
            )
        }.toImmutableList()
    }

    private fun mapFilters(filters: SmartListFilters): GlobalFilter {
        return GlobalFilter(
            mode = filters.media,
            genre = filters.genres,
            subgenre = null,
            years = filters.years.toIntPair(),
            runtime = filters.runtimes.toIntPair(),
            availability = filters.availability,
            certification = filters.certifications
                ?.mapNotNull { slug -> GlobalFilter.Certification.entries.find { it.slug == slug } }
                ?.toImmutableList(),
            region = null,
            countries = filters.countries,
            rating = filters.ratings.toIntPair(),
            hideWatched = filters.ignoreWatched,
            hideWatchlist = filters.ignoreWatchlisted,
        )
    }

    private fun List<Int>?.toIntPair(): Pair<Int, Int>? {
        val min = this?.getOrNull(0) ?: return null
        val max = getOrNull(1) ?: return null
        return min to max
    }
}
