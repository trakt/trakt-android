package tv.trakt.trakt.common.helpers.extensions

import java.util.Locale

/**
 * Language code accepted by Trakt's `language` query parameters (translations, comments).
 *
 * Android's [Locale] reports the deprecated ISO 639 codes for a handful of languages (`id` is
 * reported as `in`, `he` as `iw`, `yi` as `ji`), and Trakt files both Norwegian written standards
 * under the `no` macrolanguage. Querying with the raw code returns an empty result for those.
 */
val Locale.apiLanguage: String
    get() = when (language) {
        "in" -> "id"
        "iw" -> "he"
        "ji" -> "yi"
        "nb", "nn" -> "no"
        else -> language
    }

/**
 * Regional-indicator flag emoji for a two-letter ISO 3166 [country] code, or null when the code
 * isn't a country code the emoji range covers.
 */
fun countryFlag(country: String): String? {
    val code = country.trim().uppercase()
    if (code.length != 2 || code.any { it !in 'A'..'Z' }) {
        return null
    }

    val offset = 0x1F1E6 - 'A'.code
    return code
        .map { String(Character.toChars(offset + it.code)) }
        .joinToString(separator = "")
}
