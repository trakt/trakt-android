package tv.trakt.trakt.core.comments.features.report

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState

@Immutable
internal data class ReportCommentState(
    val loading: LoadingState = LoadingState.Idle,
    val reported: Boolean = false,
    @param:StringRes val error: Int? = null,
)
