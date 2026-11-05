package tv.trakt.trakt.core.filters.data

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode

internal interface GlobalFilterManager {
    suspend fun setFilter(filter: GlobalFilter)

    suspend fun setMode(mode: GlobalFilterMode)

    suspend fun setParentalGuideExpanded(expanded: Boolean)

    fun getFilter(): GlobalFilter

    fun getMode(): GlobalFilterMode

    fun isParentalGuideExpanded(): Boolean

    fun observeFilter(): Flow<GlobalFilter>
}
