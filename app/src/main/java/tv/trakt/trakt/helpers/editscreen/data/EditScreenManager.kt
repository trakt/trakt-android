package tv.trakt.trakt.helpers.editscreen.data

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.helpers.editscreen.data.model.EditScreenKey

internal interface EditScreenManager {
    fun isVisible(keys: Set<EditScreenKey>): Boolean

    fun observe(keys: Set<EditScreenKey>): Flow<Map<EditScreenKey, Boolean>>

    suspend fun hide(key: EditScreenKey)

    suspend fun show(key: EditScreenKey)

    suspend fun clear()
}
