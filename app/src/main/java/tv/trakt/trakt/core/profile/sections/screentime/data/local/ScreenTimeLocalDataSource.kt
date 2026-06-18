package tv.trakt.trakt.core.profile.sections.screentime.data.local

import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData

internal interface ScreenTimeLocalDataSource {
    suspend fun setData(data: ScreenTimeData)

    suspend fun getData(): ScreenTimeData?

    fun clear()
}
