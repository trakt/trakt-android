package tv.trakt.trakt.core.lists.sections.smart.templates

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.lists.SmartListTemplate

@Immutable
internal data class SmartListTemplatesState(
    val templates: ImmutableList<SmartListTemplate>? = null,
    val hidden: Boolean = false,
    val collapsed: Boolean = false,
)
