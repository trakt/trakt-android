package tv.trakt.trakt.core.comments.features.translation.data

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation.Translated
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation.Translating
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslations

/**
 * In-memory cache of on-device comment translations, shared by every comments screen
 * so a comment translated in a list stays translated in its details sheet.
 * Lives for the process lifetime only.
 */
internal class CommentTranslationsStore(
    private val translator: CommentTranslator,
) {
    private val itemsState = MutableStateFlow<PersistentMap<Int, CommentTranslation>>(persistentMapOf())
    val translations: Flow<CommentTranslations> = combine(
        flow { emit(translator.isAvailable()) },
        itemsState,
    ) { onDevice, items ->
        CommentTranslations(
            onDevice = onDevice,
            items = items,
        )
    }

    /**
     * Translates [comment] on device, or restores its original text when a translation is shown.
     * Returns false when on-device translation could not run and the caller should fall back
     * to an external translator.
     */
    suspend fun toggle(comment: Comment): Boolean {
        val current = itemsState.value[comment.id]
        if (current == Translating) {
            return true
        }
        if (current is Translated) {
            itemsState.update { it.remove(comment.id) }
            return true
        }

        if (!translator.isAvailable()) {
            return false
        }

        itemsState.update { it.put(comment.id, Translating) }

        val result = try {
            translator.translate(comment.commentNoSpoilers)
        } catch (error: CancellationException) {
            itemsState.update { it.remove(comment.id) }
            throw error
        }

        itemsState.update { items ->
            result.fold(
                onSuccess = { items.put(comment.id, Translated(it)) },
                onFailure = { items.remove(comment.id) },
            )
        }

        return result.isSuccess
    }
}
