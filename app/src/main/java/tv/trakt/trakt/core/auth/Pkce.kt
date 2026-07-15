package tv.trakt.trakt.core.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * RFC 7636 Proof Key for Code Exchange helpers for the OAuth authorization code flow.
 */
internal object Pkce {
    private const val VERIFIER_BYTE_COUNT = 32

    private val urlSafeEncoder: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()

    /**
     * A cryptographically random, URL-safe string (RFC 7636 §4.1: 43-128 characters).
     */
    fun generateCodeVerifier(): String {
        val bytes = ByteArray(VERIFIER_BYTE_COUNT)
        SecureRandom().nextBytes(bytes)
        return urlSafeEncoder.encodeToString(bytes)
    }

    /**
     * Derives the `S256` code challenge for a given verifier (RFC 7636 §4.2).
     */
    fun codeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        return urlSafeEncoder.encodeToString(digest)
    }
}
