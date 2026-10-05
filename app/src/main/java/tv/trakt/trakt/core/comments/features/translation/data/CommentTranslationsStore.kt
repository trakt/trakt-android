package tv.trakt.trakt.core.comments.features.translation.data

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
 *
 * [translators] are tried in order; the next one runs when a translator is unavailable or fails.
 * When every translator fails, the caller falls back to an external translator.
 */
internal class CommentTranslationsStore(
    private val translators: List<CommentTranslator>,
) {
    private val itemsState = MutableStateFlow<PersistentMap<Int, CommentTranslation>>(persistentMapOf())
    private val pendingDownloadState = MutableStateFlow<PendingDownload?>(null)

    /**
     * Comment waiting for the user to allow a download over a metered network.
     */
    val pendingDownload: Flow<Comment?> = pendingDownloadState.map { it?.comment }

    val translations: Flow<CommentTranslations> = combine(
        flow { emit(translators.any { it.isAvailable() }) },
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
        val current = itemsState.value[comment.id]
        if (current == Downloading || current == Translating) {
            return true
        }
        if (current is Translated) {
            itemsState.update { it.remove(comment.id) }
            return true
        }

        return translate(
            comment = comment,
            firstTranslator = 0,
            allowMeteredDownload = false,
        )
    }

    /**
     * Continues the parked translation, downloading over a metered network.
     * Returns false when it failed and the caller should fall back to an external translator.
     */
    suspend fun confirmDownload(): Boolean {
        val pending = pendingDownloadState.value ?: return true
        pendingDownloadState.update { null }

        return translate(
            comment = pending.comment,
            firstTranslator = pending.translator,
            allowMeteredDownload = true,
        )
    }

    fun cancelDownload() {
        pendingDownloadState.update { null }
    }

    private suspend fun translate(
        comment: Comment,
        firstTranslator: Int,
        allowMeteredDownload: Boolean,
    ): Boolean {
        val source = comment.language ?: return false

        try {
            for (index in firstTranslator until translators.size) {
                val translator = translators[index]
                if (!translator.isAvailable()) {
                    continue
                }

                val downloaded = translator.isDownloaded(source)
                if (!downloaded && !allowMeteredDownload && translator.isOnMeteredNetwork()) {
                    itemsState.update { it.remove(comment.id) }
                    pendingDownloadState.update {
                        PendingDownload(
                            comment = comment,
                            translator = index,
                        )
                    }
                    return true
                }

                val result = translate(
                    translator = translator,
                    comment = comment,
                    source = source,
                    downloaded = downloaded,
                )
                if (result.isSuccess) {
                    itemsState.update { it.put(comment.id, Translated(result.getOrThrow())) }
                    return true
                }
            }
        } catch (error: CancellationException) {
            itemsState.update { it.remove(comment.id) }
            throw error
        }

        itemsState.update { it.remove(comment.id) }
        return false
    }

    private suspend fun translate(
        translator: CommentTranslator,
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

private data class PendingDownload(
    val comment: Comment,
    val translator: Int,
)
