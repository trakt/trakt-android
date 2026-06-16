package tv.trakt.trakt.core.profile.sections.activity.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.ratings.UserRatingsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.profile.sections.activity.data.local.ratings.ProfileRatingsLocalDataSource
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileRatingItem

internal class GetProfileRatingsUseCase(
    private val remoteSource: UserRatingsRemoteDataSource,
    private val localDataSource: ProfileRatingsLocalDataSource,
) {
    suspend fun getLocalRatings(limit: Int): ImmutableList<ProfileRatingItem> {
        return localDataSource.getItems()
            .sortedByDescending { it.ratedAt }
            .take(limit)
            .toImmutableList()
    }

    suspend fun getRemoteRatings(pagination: Pagination): ImmutableList<ProfileRatingItem> {
        val remoteItems = remoteSource.getAllRatings(
            pagination = pagination,
        )

        return remoteItems
            .mapNotNull {
                when (it.type.value.lowercase()) {
                    "show" -> ProfileRatingItem.ShowItem(
                        show = Show.fromDto(it.show!!),
                        rating = it.rating,
                        ratedAt = it.ratedAt.toInstant(),
                    )
                    "movie" -> ProfileRatingItem.MovieItem(
                        movie = Movie.fromDto(it.movie!!),
                        rating = it.rating,
                        ratedAt = it.ratedAt.toInstant(),
                    )
                    "episode" -> ProfileRatingItem.EpisodeItem(
                        show = Show.fromDto(it.show!!),
                        episode = Episode.fromDto(it.episode!!),
                        rating = it.rating,
                        ratedAt = it.ratedAt.toInstant(),
                    )
                    else -> null
                }
            }
            .also {
                when (pagination.page) {
                    1 -> localDataSource.setItems(it)
                    else -> localDataSource.addItems(it)
                }
            }
            .toImmutableList()
    }
}
