package tv.trakt.trakt.core.filters.data

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode

internal interface GlobalFilterManager {
    suspend fun setFilter(filter: GlobalFilter)

    fun getFilter(): GlobalFilter

    fun setMode(mode: GlobalFilterMode)

    fun getMode(): GlobalFilterMode

    fun observeFilter(): Flow<GlobalFilter>
}
