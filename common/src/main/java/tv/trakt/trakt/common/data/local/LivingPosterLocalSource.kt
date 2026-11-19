package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.livingposter.LivingPoster
import tv.trakt.trakt.common.model.livingposter.LivingPosterIndex

class LivingPosterLocalSource {
    private val mutex = Mutex()
    private var index: LivingPosterIndex? = null
    private val posters = mutableMapOf<String, LivingPoster>()

    suspend fun getIndex(): LivingPosterIndex? {
        return mutex.withLock { index }
    }

    suspend fun setIndex(value: LivingPosterIndex) {
        mutex.withLock { index = value }
    }

    suspend fun getPoster(baseUrl: String): LivingPoster? {
        return mutex.withLock { posters[baseUrl] }
    }

    suspend fun setPoster(
        baseUrl: String,
        poster: LivingPoster,
    ) {
        mutex.withLock { posters[baseUrl] = poster }
    }
}
