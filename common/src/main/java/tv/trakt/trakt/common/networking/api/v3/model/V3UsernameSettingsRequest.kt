package tv.trakt.trakt.common.networking.api.v3.model

import kotlinx.serialization.Serializable

@Serializable
data class V3UsernameSettingsRequest(
    val user: User,
) {
    @Serializable
    data class User(
        val username: String,
    )
}
