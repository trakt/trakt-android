package tv.trakt.trakt.common.data.remote

import tv.trakt.trakt.common.data.dto.MixtapeDto
import tv.trakt.trakt.common.data.dto.MixtapeMediaDto
import tv.trakt.trakt.common.data.dto.MixtapeVectorsDto
import tv.trakt.trakt.common.data.request.MixtapeRequest
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.api.v3.V3RecommendationsApi

class MixtapeRemoteSource(
    private val v3RecommendationsApi: V3RecommendationsApi,
) {
    suspend fun getHistory(
        type: MediaType,
        query: String? = null,
    ): List<MixtapeMediaDto> {
        return v3RecommendationsApi.getMixtapeHistory(
            type = type,
            query = query,
        )
    }

    suspend fun getVectors(
        type: MediaType,
        ids: List<TraktId>,
    ): MixtapeVectorsDto? {
        return v3RecommendationsApi.getMixtapeVectors(
            type = type,
            ids = ids,
        )
    }

    suspend fun getMixtape(
        type: MediaType,
        picks: List<TraktId>,
        candidates: List<TraktId> = emptyList(),
        suggestions: Boolean,
    ): MixtapeDto {
        return v3RecommendationsApi.postMixtape(
            MixtapeRequest(
                type = type.value,
                picks = picks.map { it.value },
                candidates = candidates.map { it.value },
                suggestions = suggestions,
            ),
        )
    }
}
