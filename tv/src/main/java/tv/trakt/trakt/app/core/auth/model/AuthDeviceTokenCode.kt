package tv.trakt.trakt.app.core.auth.model

/**
 * Represents the state of an OAuth device token during the authentication process.
 *
 * @property value The HTTP status code associated with this state.
 */
internal enum class AuthDeviceTokenCode(
    val value: Int,
) {
    PENDING(400),
    NOT_FOUND(404),
    ALREADY_USED(409),
    EXPIRED(410),
    DENIED(418),
    TOO_MANY_REQUESTS(429),
    UNKNOWN(0),
    ;

    companion object {
        fun fromHttpCode(code: Int): AuthDeviceTokenCode {
            return entries.find { it.value == code } ?: UNKNOWN
        }
    }
}
