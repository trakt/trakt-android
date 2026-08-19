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
    return BuildConfig.SUPPORTED_LOCALES
        .map { Locale.forLanguageTag(it) }
        .distinctBy { it.apiLanguage }
        .map { locale ->
            CommentsLanguage(
                code = locale.apiLanguage,
                displayName = locale.getDisplayLanguage(locale)
                    .replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase(locale) else char.toString()
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
    val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
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

    return commentsLanguages().firstOrNull { it.code == code }?.displayName
}

/**
 * Country a language's flag stands for: the region the app's own locale tag carries (`pt-BR`,
 * `en-US`), falling back to the region ICU considers most likely for a bare language (`de` -> `DE`,
 * `ja` -> `JP`). A flag is a rough stand-in for a language, so a language spoken in many countries
 * shows a single representative one.
 */
private fun Locale.flagCountry(): String {
    return country.ifBlank {
        ULocale.addLikelySubtags(ULocale.forLocale(this)).country
    }
}
