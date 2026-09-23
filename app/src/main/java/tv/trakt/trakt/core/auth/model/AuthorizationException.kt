package tv.trakt.trakt.core.auth.model

internal class AuthorizationException(
    cause: Exception,
) : Exception("Trakt authorization failed", cause)
