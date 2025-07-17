package tv.trakt.trakt.tv.core.auth.model

import tv.trakt.trakt.tv.auth.model.TraktAccessToken

internal sealed interface AuthDeviceTokenState {
    data class Success(
        val token: TraktAccessToken,
    ) : AuthDeviceTokenState

    data class Failure(
        val code: AuthDeviceTokenCode,
    ) : AuthDeviceTokenState
}
