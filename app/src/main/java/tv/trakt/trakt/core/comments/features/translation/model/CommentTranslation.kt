package tv.trakt.trakt.core.comments.features.translation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.model.Comment

@Immutable
internal sealed interface CommentTranslation {
    data object Translating : CommentTranslation

    data class Translated(
        val text: String,
    ) : CommentTranslation
}

/**
 * On-device translations for the comments currently on screen, keyed by comment id.
 * [onDevice] is false when the device cannot run on-device translation, in which case
 * translation falls back to the Google Translate app.
 */
@Immutable
internal data class CommentTranslations(
    val onDevice: Boolean = false,
    val items: ImmutableMap<Int, CommentTranslation> = persistentMapOf(),
) {
    fun displayText(comment: Comment): String {
        return when (val translation = items[comment.id]) {
            is CommentTranslation.Translated -> translation.text
            is CommentTranslation.Translating, null -> comment.commentNoSpoilers
        }
    }
}

internal sealed interface CommentTranslationEvent {
    data class OpenExternalTranslation(
        val text: String,
    ) : CommentTranslationEvent
}
