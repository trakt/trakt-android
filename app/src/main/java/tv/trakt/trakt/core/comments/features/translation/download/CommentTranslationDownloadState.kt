package tv.trakt.trakt.core.comments.features.translation.download

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslationDownloadRequest

@Immutable
internal data class CommentTranslationDownloadState(
    val request: CommentTranslationDownloadRequest? = null,
)
