@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package tv.trakt.trakt.common.networking.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openapitools.client.models.GetCalendarsShows200ResponseInnerEpisode
import org.openapitools.client.models.GetCalendarsShows200ResponseInnerShow
import org.openapitools.client.models.GetSyncProgressUpNextStandard200ResponseInnerProgressLastEpisode
import org.openapitools.client.models.GetSyncProgressUpNextStandard200ResponseInnerProgressStats

@Serializable
data class SyncProgressItemDto(
    @SerialName(value = "show")
    val show: GetCalendarsShows200ResponseInnerShow,
    @SerialName(value = "progress")
    val progress: Progress,
) {
    /**
     *
     *
     * @param aired
     * @param completed
     * @param resetAt
     * @param nextEpisode
     * @param lastEpisode
     * @param lastWatchedAt
     * @param stats
     */
    @Serializable
    data class Progress(
        @SerialName(value = "aired")
        val aired: Int,
        @SerialName(value = "completed")
        val completed: Int,
        @SerialName(value = "reset_at")
        val resetAt: String?,
        @SerialName(value = "next_episode")
        val nextEpisode: GetCalendarsShows200ResponseInnerEpisode?,
        @SerialName(value = "last_episode")
        val lastEpisode: GetSyncProgressUpNextStandard200ResponseInnerProgressLastEpisode?,
        @SerialName(value = "last_watched_at")
        val lastWatchedAt: String? = null,
        @SerialName(value = "stats")
        val stats: GetSyncProgressUpNextStandard200ResponseInnerProgressStats? = null,
    )
}
