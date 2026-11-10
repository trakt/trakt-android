package tv.trakt.trakt.core.billing.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.networking.api.v3.model.V3UsageResponse
import tv.trakt.trakt.core.billing.model.VipUsageLimit.Companion.fromItem
import tv.trakt.trakt.resources.R

@Immutable
internal data class VipUsageGroup(
    @param:StringRes val title: Int,
    val limits: ImmutableList<VipUsageLimit>,
) {
    companion object {
        fun fromDto(dto: V3UsageResponse): ImmutableList<VipUsageGroup> {
            return persistentListOf(
                VipUsageGroup(
                    title = R.string.usage_title_lists,
                    limits = persistentListOf(
                        fromItem(R.string.limit_title_smart_lists, dto.dynamicLists),
                        fromItem(R.string.limit_title_personal_lists, dto.staticLists),
                        fromItem(R.string.limit_title_total_list_items, dto.totalListItems),
                        fromItem(R.string.limit_title_watchlist_items, dto.watchlistItems),
                    ),
                ),
                VipUsageGroup(
                    title = R.string.usage_title_library,
                    limits = persistentListOf(
                        fromItem(R.string.limit_title_digital_library, dto.digitalLibrary),
                    ),
                ),
            )
        }
    }
}
