package tv.trakt.trakt.core.filters.navigation

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter

@Immutable
@Serializable
internal data class GlobalFiltersOptions(
    val global: Boolean,
    val initial: GlobalFilter?,
) {
    companion object {
        val Default = GlobalFiltersOptions(
            global = true,
            initial = null,
        )
    }
}
