package tv.trakt.trakt.common.core.streamings.data.local

import tv.trakt.trakt.common.core.streamings.data.remote.StreamingRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.streamings.StreamingSource
import tv.trakt.trakt.common.model.streamings.fromDto

/**
 * Fills the local streaming-source cache (branding, logos, colours) when it went stale.
 * Every watchnow lookup needs it before it can map offers onto domain services.
 */
internal suspend fun StreamingLocalDataSource.cacheSourcesIfNeeded(remoteSource: StreamingRemoteDataSource) {
    if (isValid()) {
        return
    }

    val sources = remoteSource
        .getStreamingSources()
        .asyncMap { StreamingSource.fromDto(it) }

    upsertStreamingSources(sources)
}
