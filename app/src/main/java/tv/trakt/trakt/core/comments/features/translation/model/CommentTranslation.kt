package tv.trakt.trakt.core.comments.features.translation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableSet
import tv.trakt.trakt.common.model.Comment
import java.util.Locale

@Immutable
internal sealed interface CommentTranslation {
    /**
     * [progress] is a percentage, or null when the translator cannot report download progress.
     */
    data class Downloading(
        val type: CommentTranslationDownload,
        val progress: Int? = null,
    ) : CommentTranslation

    data object Translating : CommentTranslation

    /**
     * [source] is the text that was translated, so a later edit of the comment invalidates it.
     */
    data class Translated(
        val text: String,
        val source: String,
    ) : CommentTranslation
}

/**
 * What an on-device translator downloads before it can translate.
 */
internal enum class CommentTranslationDownload {
    Language,
    AiModel,
}

/**
 * A translation waiting for the user to allow a download over a metered network.
 */
@Immutable
internal data class CommentTranslationDownloadRequest(
    val comment: Comment,
    val type: CommentTranslationDownload,
)

/**
 * Source languages that can be translated on device into the app language.
 */
@Immutable
internal sealed interface OnDeviceLanguages {
    data object None : OnDeviceLanguages

    data object All : OnDeviceLanguages

    /**
     * [languages] are language codes as returned by [languageCode].
     */
    data class Some(
        val languages: ImmutableSet<String>,
    ) : OnDeviceLanguages

    fun supports(language: Locale): Boolean {
        return when (this) {
            None -> false
            All -> true
            is Some -> language.languageCode() in languages
        }
    }

    operator fun plus(other: OnDeviceLanguages): OnDeviceLanguages {
        return when {
            this == All || other == All -> All
            this is Some && other is Some -> Some((languages + other.languages).toImmutableSet())
            this is Some -> this
            else -> other
        }
    }
}

/**
 * Language code without region or script, with legacy codes such as "iw" mapped to "he".
 */
internal fun Locale.languageCode(): String {
    return Locale.forLanguageTag(language).toLanguageTag()
}

/**
 * On-device translations for the comments currently on screen, keyed by comment id.
 * Comments in a language outside [languages] fall back to the Google Translate app.
 */
@Immutable
internal data class CommentTranslations(
    val languages: OnDeviceLanguages = OnDeviceLanguages.None,
    val items: ImmutableMap<Int, CommentTranslation> = persistentMapOf(),
) {
    fun supportsOnDevice(comment: Comment): Boolean {
        val language = comment.language ?: return false
        return languages.supports(language)
    }

    /**
     * Returns null for a translation of text the comment no longer has, after an edit.
     */
    fun translation(comment: Comment): CommentTranslation? {
        val translation = items[comment.id]
        if (translation is CommentTranslation.Translated && !translation.isOf(comment)) {
            return null
        }
        return translation
    }

    fun displayText(comment: Comment): String {
        return when (val translation = translation(comment)) {
            is CommentTranslation.Translated -> translation.text
            is CommentTranslation.Downloading, is CommentTranslation.Translating, null -> comment.commentNoSpoilers
        }
    }
}

internal fun CommentTranslation.Translated.isOf(comment: Comment): Boolean {
    return source == comment.commentNoSpoilers
}

internal sealed interface CommentTranslationEvent {
    data class OpenExternalTranslation(
        val text: String,
    ) : CommentTranslationEvent
}
