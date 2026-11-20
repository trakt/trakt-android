package tv.trakt.trakt.common.data.local

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.lists.AwardCeremony
import tv.trakt.trakt.common.model.lists.SmartListLanguage

/** In-memory catalog of smart list filter options, kept for the process lifetime. */
class SmartListFilterOptionsLocalSource {
    private val mutex = Mutex()
    private var awardCatalog: ImmutableList<AwardCeremony>? = null
    private val languages = mutableMapOf<MediaMode, ImmutableList<SmartListLanguage>>()

    suspend fun getAwardCatalog(): ImmutableList<AwardCeremony>? = mutex.withLock { awardCatalog }

    suspend fun setAwardCatalog(catalog: ImmutableList<AwardCeremony>) {
        mutex.withLock { awardCatalog = catalog }
    }

    suspend fun getLanguages(media: MediaMode): ImmutableList<SmartListLanguage>? = mutex.withLock { languages[media] }

    suspend fun setLanguages(
        media: MediaMode,
        values: ImmutableList<SmartListLanguage>,
    ) {
        mutex.withLock { languages[media] = values }
    }
}
