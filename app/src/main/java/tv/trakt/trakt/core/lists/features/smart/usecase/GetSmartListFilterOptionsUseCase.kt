package tv.trakt.trakt.core.lists.features.smart.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.data.local.SmartListFilterOptionsLocalSource
import tv.trakt.trakt.common.data.remote.SmartListFilterOptionsRemoteSource
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.lists.AwardCeremony
import tv.trakt.trakt.common.model.lists.SmartListLanguage

internal class GetSmartListFilterOptionsUseCase(
    private val remoteSource: SmartListFilterOptionsRemoteSource,
    private val localSource: SmartListFilterOptionsLocalSource,
) {
    suspend fun getAwardCatalog(): ImmutableList<AwardCeremony> {
        localSource.getAwardCatalog()?.let { return it }

        return remoteSource.getAwardCatalog()
            .map(AwardCeremony::fromDto)
            .toImmutableList()
            .also { localSource.setAwardCatalog(it) }
    }

    suspend fun getLanguages(media: MediaMode): ImmutableList<SmartListLanguage> {
        localSource.getLanguages(media)?.let { return it }

        val types = when (media) {
            MediaMode.Media -> listOf("movies", "shows")
            MediaMode.Movies -> listOf("movies")
            MediaMode.Shows -> listOf("shows")
        }

        val languages = coroutineScope {
            types
                .map { type -> async { remoteSource.getLanguages(type) } }
                .awaitAll()
        }

        return languages
            .flatten()
            .map(SmartListLanguage::fromDto)
            .distinctBy { it.code }
            .sortedBy { it.name }
            .toImmutableList()
            .also { localSource.setLanguages(media, it) }
    }
}
