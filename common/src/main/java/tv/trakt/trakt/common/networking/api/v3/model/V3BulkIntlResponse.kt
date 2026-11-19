package tv.trakt.trakt.common.networking.api.v3.model

import kotlinx.serialization.Serializable

@Serializable
data class V3BulkIntlResponse(
    val movie: Map<String, V3IntlTitle> = emptyMap(),
    val show: Map<String, V3IntlTitle> = emptyMap(),
    val episode: Map<String, V3IntlTitle> = emptyMap(),
) {
    @Serializable
    data class V3IntlTitle(
        val title: String? = null,
    )
}
