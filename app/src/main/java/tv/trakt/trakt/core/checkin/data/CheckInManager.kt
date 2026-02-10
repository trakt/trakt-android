package tv.trakt.trakt.core.checkin.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.checkin.model.CheckInState

interface CheckInManager {
    suspend fun startMovie(movieId: TraktId)

    suspend fun checkActive(context: Context)

    fun observe(): Flow<CheckInState>

    suspend fun stop(context: Context)

//    suspend fun clear(context: Context)
}
