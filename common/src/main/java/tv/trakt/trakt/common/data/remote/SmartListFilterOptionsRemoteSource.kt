package tv.trakt.trakt.common.data.remote

import org.openapitools.client.apis.AwardsApi
import org.openapitools.client.apis.LanguagesApi
import tv.trakt.trakt.common.networking.AwardCeremonyDto
import tv.trakt.trakt.common.networking.LanguageDto

class SmartListFilterOptionsRemoteSource(
    private val awardsApi: AwardsApi,
    private val languagesApi: LanguagesApi,
) {
    suspend fun getAwardCatalog(): List<AwardCeremonyDto> {
        return awardsApi.getAwardsCatalog().body()
    }

    suspend fun getLanguages(type: String): List<LanguageDto> {
        return languagesApi.getLanguagesList(type = type).body()
    }
}
