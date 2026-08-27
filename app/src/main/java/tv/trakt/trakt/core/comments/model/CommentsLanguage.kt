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
 * Languages the app itself is localised into, grouped by API language code so regional variants
 * (`en-US` / `en-AU`, `pt-BR` / `pt-PT`) collapse into a single option.
 *
 * When multiple regional variants exist, the variant matching the active app locale is preferred
 * for the display name and representative flag. If no exact regional match exists, the first
 * supported variant is used.
 */
internal fun commentsLanguages(
    appLocale: Locale = activeAppLocale(),
): ImmutableList<CommentsLanguage> {
    return BuildConfig.SUPPORTED_LOCALES
        .map { Locale.forLanguageTag(it) }
        .groupBy { it.apiLanguage }
        .values
        .map { variants ->
            variants.firstOrNull { locale ->
                locale.language == appLocale.language &&
                    locale.country == appLocale.country
            } ?: variants.first()
        }
        .map { locale ->
            CommentsLanguage(
                code = locale.apiLanguage,
                // Use the regional variant matching the active app locale whenever possible.
                displayName = locale.getDisplayLanguage(locale)
                    .replaceFirstChar { char ->
                        if (char.isLowerCase()) {
                            char.titlecase(locale)
                        } else {
                            char.toString()
                        }
                    },
                flag = countryFlag(locale.flagCountry()),
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

    if (commentsLanguages(locale).none { it.code == code }) return null

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
 * Country represented by a language's selected regional locale. The locale matching the active
 * app language is selected before this function is called. For bare language tags (`de`, `ja`),
 * ICU provides the most likely region.
 *
 * A flag is only a visual representation of the language option; the API code remains
 * language-only, such as `pt`.
 */
private fun Locale.flagCountry(): String {
    return country.ifBlank {
        ULocale.addLikelySubtags(ULocale.forLocale(this)).country
    }
}
