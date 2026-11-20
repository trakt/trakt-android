package tv.trakt.trakt.core.lists.features.smart.search

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.extensions.capitalize

@Immutable
internal data class SmartListSearchOption(
    val value: String,
    val label: String,
)

internal fun String.toSearchValueLabel(): String {
    return split('-', '_', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { it.capitalize() }
}
