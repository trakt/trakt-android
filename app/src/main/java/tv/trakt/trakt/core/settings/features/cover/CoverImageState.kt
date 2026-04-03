package tv.trakt.trakt.core.settings.features.cover

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User

@Immutable
internal data class CoverImageState(
    val mediaId: TraktId,
    val mediaTitle: String,
    val mediaType: MediaType,
    val mediaImage: String?,
    val user: User? = null,
    val loading: LoadingState = Idle,
    val error: Exception? = null,
)
