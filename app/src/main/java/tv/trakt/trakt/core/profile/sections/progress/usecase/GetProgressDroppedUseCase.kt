package tv.trakt.trakt.core.profile.sections.progress.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.UserRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.profile.sections.progress.data.local.dropped.ProgressDroppedLocalSource
import tv.trakt.trakt.core.profile.sections.progress.model.ProfileProgressItem
import tv.trakt.trakt.core.profile.sections.progress.model.ProfileProgressItem.ShowItem

internal class GetProgressDroppedUseCase(
    private val remoteUserSource: UserRemoteSource,
    private val localDataSource: ProgressDroppedLocalSource,
) {
    suspend fun getLocalDropped(limit: Int): ImmutableList<ProfileProgressItem> {
        return localDataSource.getItems()
            .filterIsInstance<ShowItem>()
            .sortedByDescending { it.hiddenAt }
            .take(limit)
            .toImmutableList()
    }

    suspend fun getDropped(
        page: Int,
        limit: Int,
    ): ImmutableList<ProfileProgressItem> {
        val shows = remoteUserSource.getDroppedShows(
            page = page,
            limit = limit,
        )

        return shows
            .sortedByDescending { it.hiddenAt }
            .asyncMap {
                ShowItem(
                    show = Show.fromDto(it.show),
                    hiddenAt = it.hiddenAt?.toInstant(),
                )
            }
            .also {
                when (page) {
                    1 -> localDataSource.setItems(it)
                    else -> localDataSource.addItems(it)
                }
            }
            .toImmutableList()
    }
}
