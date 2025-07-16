package tv.trakt.app.tv.core.auth.model

import tv.trakt.app.tv.auth.model.TraktAccessToken

internal sealed interface AuthDeviceTokenState {
    data class Success(
        val token: TraktAccessToken,
    ) : AuthDeviceTokenState

    data class Failure(
        val code: AuthDeviceTokenCode,
    ) : AuthDeviceTokenState
}
