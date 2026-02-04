package tv.trakt.trakt.core.checkin

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.checkin.model.CheckInState

interface CheckInManager {
    suspend fun startMovie(movieId: TraktId)

    suspend fun checkActive()

    fun observe(): Flow<CheckInState>

    suspend fun stop()
}
