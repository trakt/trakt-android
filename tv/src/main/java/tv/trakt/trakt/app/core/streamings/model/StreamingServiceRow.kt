package tv.trakt.trakt.app.core.streamings.model

import tv.trakt.trakt.app.common.model.StreamingService

internal data class StreamingServiceRow(
    val source: String = "",
    val services: List<StreamingService> = emptyList(),
)
