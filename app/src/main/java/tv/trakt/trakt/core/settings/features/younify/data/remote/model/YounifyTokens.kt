package tv.trakt.trakt.core.settings.features.younify.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
internal data class YounifyTokens(
    val accessToken: String,
    val refreshToken: String,
)
