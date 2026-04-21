@file:Suppress(
    "ArrayInDataClass",
    "DuplicatedCode",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "RemoveRedundantCallsOfConversionMethods",
    "REDUNDANT_CALL_OF_CONVERSION_METHOD",
    "RedundantUnitReturnType",
    "RemoveEmptyClassBody",
    "UnnecessaryVariable",
    "UnusedImport",
    "UnnecessaryVariable",
    "unused",
)

package org.openapitools.client.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 *
 * @param progress
 * @param pausedAt
 * @param id
 * @param type
 */
@Serializable
data class GetSyncProgressEpisodesResponse(
    @Contextual @SerialName(value = "progress")
    val progress: Float,
    @SerialName(value = "paused_at")
    val pausedAt: String,
    @SerialName(value = "id")
    val id: Long,
    @SerialName(value = "type")
    val type: GetSyncProgressEpisodesResponse.Type,
    val episode: GetSyncProgressEpisodesResponse.Episode,
    val show: GetSyncProgressEpisodesResponse.Show,
) {
    /**
     *
     *
     * Values: EPISODE
     */
    @Serializable
    enum class Type(
        val value: String,
    ) {
        @SerialName(value = "episode")
        EPISODE("episode"),
    }

    @Serializable
    data class Episode(
        @SerialName(value = "ids")
        val ids: Ids,
    ) {
        @Serializable
        data class Ids(
            @SerialName(value = "trakt")
            val trakt: Int,
        )
    }

    @Serializable
    data class Show(
        @SerialName(value = "ids")
        val ids: Ids,
    ) {
        @Serializable
        data class Ids(
            @SerialName(value = "trakt")
            val trakt: Int,
        )
    }
}
