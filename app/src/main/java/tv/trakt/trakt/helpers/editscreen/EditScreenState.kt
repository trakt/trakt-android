package tv.trakt.trakt.helpers.editscreen

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.helpers.editscreen.data.model.EditScreenKey

@Immutable
internal data class EditScreenState(
    val values: ImmutableMap<EditScreenKey, Boolean>? = null,
)
