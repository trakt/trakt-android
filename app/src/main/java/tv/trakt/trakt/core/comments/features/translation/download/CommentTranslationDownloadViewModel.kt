package tv.trakt.trakt.core.comments.features.translation.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.core.comments.features.translation.data.CommentTranslationsStore

/**
 * Hosted at the app root so a confirmed download keeps running after the user leaves the comments screen.
 */
@Suppress("UNCHECKED_CAST")
internal class CommentTranslationDownloadViewModel(
    private val translationsStore: CommentTranslationsStore,
) : ViewModel() {
    private val initialState = CommentTranslationDownloadState()

    fun confirmDownload() {
        viewModelScope.launch {
            translationsStore.confirmDownload()
        }
    }

    fun cancelDownload() {
        translationsStore.cancelDownload()
    }

    val state = combine(
        translationsStore.pendingDownload,
    ) { state ->
        CommentTranslationDownloadState(
            comment = state[0] as Comment?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
