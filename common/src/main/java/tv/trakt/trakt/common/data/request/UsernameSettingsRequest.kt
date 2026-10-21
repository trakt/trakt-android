package tv.trakt.trakt.common.data.request

import kotlinx.serialization.Serializable

@Serializable
data class UsernameSettingsRequest(
    val user: User,
) {
    @Serializable
    data class User(
        val username: String,
    )
}
