package tv.trakt.trakt.core.main.helpers

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.core.main.model.MediaMode

internal interface MediaModeManager {
    suspend fun setMode(mode: MediaMode)

    fun getMode(): MediaMode

    fun observeMode(): Flow<MediaMode>
}
