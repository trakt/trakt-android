package tv.trakt.trakt.core.profile.sections.screentime.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData

internal class ScreenTimeStorage : ScreenTimeLocalDataSource {
    private val mutex = Mutex()
    private var storage: ScreenTimeData? = null

    override suspend fun setData(data: ScreenTimeData) {
        mutex.withLock {
            storage = data
        }
    }

    override suspend fun getData(): ScreenTimeData? {
        return mutex.withLock {
            storage
        }
    }

    override fun clear() {
        storage = null
    }
}
