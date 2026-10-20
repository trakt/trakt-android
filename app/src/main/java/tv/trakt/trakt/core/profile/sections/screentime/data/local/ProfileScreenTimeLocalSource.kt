package tv.trakt.trakt.core.profile.sections.screentime.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.profile.ScreenTimeData

internal class ProfileScreenTimeLocalSource {
    private val mutex = Mutex()
    private var storage: ScreenTimeData? = null

    suspend fun setData(data: ScreenTimeData) {
        mutex.withLock {
            storage = data
        }
    }

    suspend fun getData(): ScreenTimeData? {
        return mutex.withLock {
            storage
        }
    }

    fun clear() {
        storage = null
    }
}
