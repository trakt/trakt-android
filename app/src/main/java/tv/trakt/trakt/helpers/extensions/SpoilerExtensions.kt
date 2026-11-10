package tv.trakt.trakt.helpers.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.ui.composables.LocalSpoilerState

@Composable
@ReadOnlyComposable
internal fun Episode.isSpoilerHidden(): Boolean {
    return LocalSpoilerState.current.isEpisodeHidden(ids.trakt)
}

@Composable
internal fun Episode.spoilerFreeTitle(): String {
    return when {
        isSpoilerHidden() -> seasonEpisodeString()
        else -> title
    }
}
