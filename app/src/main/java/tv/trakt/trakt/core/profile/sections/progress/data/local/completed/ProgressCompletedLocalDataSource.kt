package tv.trakt.trakt.core.profile.sections.progress.data.local.completed

import tv.trakt.trakt.core.profile.sections.progress.model.ProfileProgressItem

internal interface ProgressCompletedLocalDataSource {
    suspend fun addItems(items: List<ProfileProgressItem>)

    suspend fun setItems(items: List<ProfileProgressItem>)

    suspend fun getItems(): List<ProfileProgressItem>

    fun clear()
}
