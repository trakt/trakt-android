package tv.trakt.trakt.core.comments.features.translation.data

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation.Downloading
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation.Translated
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation.Translating
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslations
import java.util.Locale

/**
 * In-memory cache of on-device comment translations, shared by every comments screen
 * so a comment translated in a list stays translated in its details sheet.
 * Lives for the process lifetime only.
 */
internal class CommentTranslationsStore(
    private val translator: CommentTranslator,
) {
    private val itemsState = MutableStateFlow<PersistentMap<Int, CommentTranslation>>(persistentMapOf())
    private val pendingDownloadState = MutableStateFlow<Comment?>(null)

    /**
     * Comment waiting for the user to allow a language download over a metered network.
     */
    val pendingDownload: StateFlow<Comment?> = pendingDownloadState.asStateFlow()

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
     * When a language download is needed over a metered network, the comment is parked in
     * [pendingDownload] until [confirmDownload] or [cancelDownload] is called.
     * Returns false when on-device translation could not run and the caller should fall back
     * to an external translator.
     */
    suspend fun toggle(comment: Comment): Boolean {
        return toggle(
            comment = comment,
            allowMeteredDownload = false,
        )
    }

    suspend fun confirmDownload() {
        val comment = pendingDownloadState.value ?: return
        pendingDownloadState.update { null }

        toggle(
            comment = comment,
            allowMeteredDownload = true,
        )
    }

    fun cancelDownload() {
        pendingDownloadState.update { null }
    }

    private suspend fun toggle(
        comment: Comment,
        allowMeteredDownload: Boolean,
    ): Boolean {
        val current = itemsState.value[comment.id]
        if (current == Downloading || current == Translating) {
            return true
        }
        if (current is Translated) {
            itemsState.update { it.remove(comment.id) }
            return true
        }

        val source = comment.language
        if (source == null || !translator.isAvailable()) {
            return false
        }

        val downloaded = translator.isDownloaded(source)
        if (!downloaded && !allowMeteredDownload && translator.isOnMeteredNetwork()) {
            pendingDownloadState.update { comment }
            return true
        }

        val result = try {
            translate(
                comment = comment,
                source = source,
                downloaded = downloaded,
            )
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

    private suspend fun translate(
        comment: Comment,
        source: Locale,
        downloaded: Boolean,
    ): Result<String> {
        if (!downloaded) {
            itemsState.update { it.put(comment.id, Downloading) }

            translator.download(source).onFailure {
                return Result.failure(it)
            }
        }

        itemsState.update { it.put(comment.id, Translating) }

        return translator.translate(
            text = comment.commentNoSpoilers,
            source = source,
        )
    }
}
