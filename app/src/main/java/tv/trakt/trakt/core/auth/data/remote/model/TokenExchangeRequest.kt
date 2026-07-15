package tv.trakt.trakt.core.auth.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body for the `authorization_code` grant on `oauth/token`.
 *
 * Defined locally (instead of the generated `PostOauthTokenRequest`) because the OpenAPI
 * spec does not yet describe the PKCE `code_verifier` field (RFC 7636).
 */
@Serializable
internal data class TokenExchangeRequest(
    @SerialName("code")
    val code: String,
    @SerialName("client_id")
    val clientId: String,
    @SerialName("client_secret")
    val clientSecret: String,
    @SerialName("redirect_uri")
    val redirectUri: String,
    /**
     * PKCE verifier matching the `code_challenge` sent with the authorization request.
     * Only applies to the `authorization_code` grant - the refresh grant carries no code
     * and has no corresponding challenge to prove against.
     */
    @SerialName("code_verifier")
    val codeVerifier: String?,
    @SerialName("grant_type")
    val grantType: String,
)
