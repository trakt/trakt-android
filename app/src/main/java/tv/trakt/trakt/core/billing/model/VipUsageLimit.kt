package tv.trakt.trakt.core.billing.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.networking.api.v3.model.V3UsageResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3UsageResponse.V3UsageItem
import tv.trakt.trakt.resources.R

@Immutable
internal data class VipUsageLimit(
    @param:StringRes val title: Int,
    val current: Int,
    val free: Int,
    val vip: Int,
) {
    companion object {
        fun fromDto(dto: V3UsageResponse): ImmutableList<VipUsageLimit> {
            return persistentListOf(
                fromItem(R.string.limit_title_total_list_items, dto.totalListItems),
                fromItem(R.string.limit_title_watchlist_items, dto.watchlistItems),
                fromItem(R.string.limit_title_smart_lists, dto.dynamicLists),
                fromItem(R.string.limit_title_personal_lists, dto.staticLists),
                fromItem(R.string.limit_title_digital_library, dto.digitalLibrary),
            )
        }

        private fun fromItem(
            @StringRes title: Int,
            item: V3UsageItem,
        ): VipUsageLimit {
            return VipUsageLimit(
                title = title,
                current = item.current ?: 0,
                free = item.free ?: 0,
                vip = item.vip ?: 0,
            )
        }
    }
}
