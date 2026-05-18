package tv.trakt.trakt.core.filters

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode

@Immutable
internal data class GlobalFiltersState(
    val mode: GlobalFilterMode? = null,
    val filter: GlobalFilter = GlobalFilter.Default,
)
