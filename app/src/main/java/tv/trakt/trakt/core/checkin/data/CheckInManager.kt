package tv.trakt.trakt.core.checkin.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates
import tv.trakt.trakt.core.checkin.model.CheckInState

internal interface CheckInManager {
    suspend fun startMovie(
        movieId: TraktId,
        source: CheckInUpdates.Source,
        context: Context,
    )

    suspend fun startEpisode(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        source: CheckInUpdates.Source,
        context: Context,
    )

    suspend fun checkActive(context: Context)

    fun isActive(): Boolean

    fun observe(): Flow<CheckInState>

    suspend fun stop(
        source: CheckInUpdates.Source,
        context: Context,
    )
}
