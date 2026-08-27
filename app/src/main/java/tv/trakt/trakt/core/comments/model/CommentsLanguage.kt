package tv.trakt.trakt.core.comments.model

import android.icu.util.ULocale
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.common.helpers.extensions.apiLanguage
import tv.trakt.trakt.common.helpers.extensions.countryFlag
import java.util.Locale

/**
 * A single language option for the comments language filter. [code] is the code Trakt accepts in
 * the `language` query parameter, which is not always the code Android's [Locale] reports - see
 * [apiLanguage].
 */
@Immutable
internal data class CommentsLanguage(
    val code: String,
    val displayName: String,
    val flag: String?,
)

/**
 * Languages the app itself is localised into, de-duplicated by API language code so regional
 * variants (`en-US` / `en-AU`, `pt-BR` / `pt-PT`) collapse into a single option, sorted by display
 * name.
 */
internal fun commentsLanguages(): ImmutableList<CommentsLanguage> {
    val appLocale = activeAppLocale()

    return BuildConfig.SUPPORTED_LOCALES
        .map { Locale.forLanguageTag(it) }
        .distinctBy { it.apiLanguage }
        .map { locale ->
            CommentsLanguage(
                code = locale.apiLanguage,
                // Display name comes from the app locale, not from the API code: `nb` maps to the
                // `no` macrolanguage, which would otherwise read as the generic "norsk".
                displayName = locale.getDisplayLanguage(locale)
                    .replaceFirstChar { char ->
                        if (char.isLowerCase()) {
                            char.titlecase(locale)
                        } else {
                            char.toString()
                        }
                    },
                flag = countryFlag(locale.flagCountry(appLocale)),
            )
        }
        .sortedBy { it.displayName }
        .toImmutableList()
}

/**
 * The app's own language as a comments language code, or null when comments cannot be filtered by it
 * - a system locale the app isn't localised into would otherwise filter every comment away.
 */
internal fun appCommentsLanguage(): String? {
    val locale = activeAppLocale()
    val code = locale.apiLanguage

    if (commentsLanguages().none { it.code == code }) return null

    return code
}

/**
 * Display name for a stored language [code], or null when the code is unknown or absent
 * (absent means "all languages").
 */
internal fun commentsLanguageDisplayName(code: String?): String? {
    if (code == null) return null

    return commentsLanguages()
        .firstOrNull { it.code == code }
        ?.displayName
}

/**
 * Returns the active locale selected for the application. When no per-app language has been
 * selected, the application follows the system locale.
 */
private fun activeAppLocale(): Locale {
    return AppCompatDelegate.getApplicationLocales().get(0)
        ?: Locale.getDefault()
}

/**
 * Returns the representative country for a language flag.
 *
 * When the language matches the active app language and the app locale includes
 * a region, that region is used for the flag. Otherwise, the supported locale's
 * region is preserved, falling back to ICU for locale tags without a country.
 */
private fun Locale.flagCountry(appLocale: Locale): String {
    return when {
        language == appLocale.language && appLocale.country.isNotBlank() -> {
            appLocale.country
        }

        else -> {
            country.ifBlank {
                ULocale.addLikelySubtags(ULocale.forLocale(this)).country
            }
        }
    }
}
