package tv.trakt.trakt.core.comments.features.translation.download

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Comment

@Immutable
internal data class CommentTranslationDownloadState(
    val comment: Comment? = null,
)
