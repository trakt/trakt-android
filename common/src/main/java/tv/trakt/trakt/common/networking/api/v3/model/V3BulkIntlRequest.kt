package tv.trakt.trakt.common.networking.api.v3.model

import tv.trakt.trakt.common.model.TraktId

data class V3BulkIntlRequest(
    val language: String,
    val country: String,
    val movieIds: List<TraktId>,
    val showIds: List<TraktId>,
    val episodeIds: List<TraktId>,
)
