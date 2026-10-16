package tv.trakt.trakt.common.auth.model

import tv.trakt.trakt.common.auth.model.TraktAccessToken

sealed interface AuthDeviceTokenState {
    data class Success(
        val token: TraktAccessToken,
    ) : AuthDeviceTokenState

    data class Failure(
        val code: AuthDeviceTokenCode,
    ) : AuthDeviceTokenState
}
