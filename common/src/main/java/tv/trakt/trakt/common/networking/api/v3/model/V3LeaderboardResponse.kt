package tv.trakt.trakt.common.networking.api.v3.model

data class V3LeaderboardResponse(
    val entries: List<V3LeaderboardEntryResponse>,
    val totalCount: Int?,
)
