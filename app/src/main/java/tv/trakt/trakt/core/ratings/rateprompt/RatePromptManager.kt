package tv.trakt.trakt.core.ratings.rateprompt

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptMedia
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState

internal interface RatePromptManager {
    suspend fun checkMovies()

    suspend fun onUserDismiss(
        movieId: TraktId,
        hasRated: Boolean,
        hasMoreMedia: List<RatePromptMedia>,
    )

    suspend fun onUserSuppress()

    fun observe(): Flow<RatePromptState>

    fun clear()
}
