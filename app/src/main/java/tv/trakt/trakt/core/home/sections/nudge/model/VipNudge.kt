package tv.trakt.trakt.core.home.sections.nudge.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.networking.api.v3.model.V3NudgeResponse
import tv.trakt.trakt.common.networking.api.v3.model.V3UsageResponse

internal enum class VipNudgeLimit(
    val value: String,
) {
    WatchlistItems("watchlist_items"),
    TotalListItems("total_list_items"),
    StaticLists("static_lists"),
    TotalNotes("total_notes"),
    ;

    fun vipAmount(usage: V3UsageResponse): Int? {
        return when (this) {
            WatchlistItems -> usage.watchlistItems.vip
            TotalListItems -> usage.totalListItems.vip
            StaticLists -> usage.staticLists.vip
            TotalNotes -> usage.totalNotes.vip
        }
    }
}

@Immutable
internal sealed interface VipNudge {
    @Immutable
    data class Features(
        val vipWatchlistItems: Int,
    ) : VipNudge

    @Immutable
    data class Limit(
        val limit: VipNudgeLimit,
        val used: Int,
        val total: Int,
        val vip: Int?,
    ) : VipNudge {
        val isOverLimit: Boolean
            get() = used > total

        val fill: Float
            get() {
                val capacity = maxOf(used, total)
                return if (capacity > 0) used.toFloat() / capacity else 0F
            }

        val base: Int
            get() = minOf(used, total)

        val over: Int
            get() = maxOf(used - total, 0)
    }

    companion object {
        fun fromDto(
            dto: V3NudgeResponse?,
            usage: V3UsageResponse,
        ): VipNudge? {
            val nudge = dto?.nudge ?: return null

            return when (nudge.type) {
                "vip" -> {
                    val vipWatchlistItems = usage.watchlistItems.vip ?: return null
                    Features(vipWatchlistItems = vipWatchlistItems)
                }
                "limit" -> {
                    val limit = VipNudgeLimit.entries.firstOrNull { it.value == nudge.limit } ?: return null
                    val used = nudge.used ?: return null
                    val total = nudge.total ?: return null
                    val vip = limit.vipAmount(usage)
                    if (vip == null && limit != VipNudgeLimit.StaticLists) return null
                    Limit(
                        limit = limit,
                        used = used,
                        total = total,
                        vip = vip,
                    )
                }
                else -> {
                    null
                }
            }
        }
    }
}
