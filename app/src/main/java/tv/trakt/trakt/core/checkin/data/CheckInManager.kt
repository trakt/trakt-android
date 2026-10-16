package tv.trakt.trakt.core.checkin.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.checkin.model.CheckInState

internal interface CheckInManager {
    suspend fun startMovie(
        movieId: TraktId,
        origin: ChangeOrigin? = null,
        context: Context,
    )

    suspend fun startEpisode(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        origin: ChangeOrigin? = null,
        context: Context,
    )

    suspend fun checkActive(context: Context)

    fun isActive(): Boolean

    fun isActiveMovie(): CheckInState.ActiveMovie?

    fun observe(): Flow<CheckInState>

    suspend fun stop(
        origin: ChangeOrigin? = null,
        context: Context,
    )
}
