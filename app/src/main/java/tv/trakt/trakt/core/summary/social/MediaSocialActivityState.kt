package tv.trakt.trakt.core.summary.social

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity

@Immutable
internal data class MediaSocialActivityState(
    val activity: ImmutableList<MediaSocialActivity>? = null,
)
