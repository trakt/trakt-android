package tv.trakt.trakt.common.ui.composables

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import tv.trakt.trakt.common.core.user.SpoilerState
import tv.trakt.trakt.common.model.MediaType

val LocalSpoilerState = compositionLocalOf { SpoilerState() }
val LocalSpoilerReveal = staticCompositionLocalOf<(MediaType) -> Unit> { {} }
