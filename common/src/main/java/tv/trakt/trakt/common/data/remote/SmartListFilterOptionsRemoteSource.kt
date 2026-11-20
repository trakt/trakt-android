package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.AwardsApi
import org.openapitools.client.apis.LanguagesApi
import tv.trakt.trakt.common.data.dto.SmartListFilterOptionDto
import tv.trakt.trakt.common.networking.AwardCeremonyDto
import tv.trakt.trakt.common.networking.LanguageDto
import tv.trakt.trakt.common.networking.api.v3.V3Api

class SmartListFilterOptionsRemoteSource(
    private val awardsApi: AwardsApi,
    private val languagesApi: LanguagesApi,
    private val v3Api: V3Api,
) {
    suspend fun getAwardCatalog(): List<AwardCeremonyDto> {
        return awardsApi.getAwardsCatalog().body()
    }

    suspend fun getFilterOptions(
        kind: String,
        query: String,
        limit: Int,
    ): List<SmartListFilterOptionDto> {
        return v3Api.getSmartListFilterOptions(
            kind = kind,
            query = query,
            limit = limit,
        )
    }

    suspend fun getLanguages(type: String): List<LanguageDto> {
        return languagesApi.getLanguagesList(type = type).body()
    }
}
