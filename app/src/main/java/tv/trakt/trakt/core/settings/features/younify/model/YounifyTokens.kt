package tv.trakt.trakt.core.settings.features.younify.model

import kotlinx.serialization.Serializable

@Serializable
internal data class YounifyTokens(
    val accessToken: String,
    val refreshToken: String,
)
