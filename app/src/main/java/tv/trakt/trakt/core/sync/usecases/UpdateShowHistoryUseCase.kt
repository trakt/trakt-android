package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult

internal class UpdateShowHistoryUseCase(
    private val remoteSource: ShowsSyncRemoteDataSource,
) {
    suspend fun addToWatched(
        showId: TraktId,
        customDate: DateSelectionResult? = null,
    ) {
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        remoteSource.addToWatched(
            showId = showId,
            watchedAt = watchedAt,
        )
    }

    suspend fun removeAllFromHistory(showId: TraktId) {
        remoteSource.removeAllFromHistory(
            showId = showId,
        )
    }

    suspend fun dropShow(showId: TraktId) {
        remoteSource.dropShow(showId)
    }
}
