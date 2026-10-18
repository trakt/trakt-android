package tv.trakt.trakt.common.firebase.analytics.implementation.hal

import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_ACTION
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_ID
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_MODE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_OUTCOME
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_PLAN
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_ROUTE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_SOURCE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_THEME
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_TYPE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_BLOCK
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_CALENDAR_PERIOD
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_COVER_IMAGE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_DISCOVER_MODE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_FILTER
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_FOLLOW
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_PAGE_VIEW
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_SEARCH
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_SHARE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_SHARE_ARRIVAL
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_THEME
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_VIP_UPGRADE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_VIP_UPSELL

private const val MEDIA_MODE_TOGGLE_SOURCE = "media-mode-toggle"
private const val USER_PROPERTY_VIP = "vip"

internal class HalAnalytics(
    private val events: HalEvents,
    override val reactions: Analytics.Reactions,
    override val ratings: Analytics.Ratings,
    override val comments: Analytics.Comments,
    override val progress: Analytics.Progress,
    override val trivia: Analytics.Trivia,
    override val playback: Analytics.Playback,
    override val lists: Analytics.Lists,
    override val today: Analytics.Today,
) : Analytics {
    override fun setUserId(userId: String?) = Unit

    override fun setUserProperty(
        key: String,
        value: String?,
    ) {
        if (key != USER_PROPERTY_VIP) return
        events.setUserType(halUserType(value))
    }

    override fun logScreenView(screenName: String) {
        events.track(EVENT_PAGE_VIEW, dims = mapOf(DIM_ROUTE to halRoute(screenName)))
    }

    override fun logUserLogin(source: String) = Unit

    override fun logUserLogout() = Unit

    override fun logMediaModeClick(mode: String) {
        val mediaMode = halMediaMode(mode) ?: return
        events.track(
            EVENT_DISCOVER_MODE,
            dims = mapOf(
                DIM_SOURCE to MEDIA_MODE_TOGGLE_SOURCE,
                DIM_MODE to mediaMode,
            ),
        )
    }

    override fun logMediaMode(mode: String) = Unit

    override fun logShareArrival(
        type: String,
        result: String,
    ) {
        events.track(
            EVENT_SHARE_ARRIVAL,
            dims = mapOf(
                DIM_TYPE to type.lowercase(),
                DIM_OUTCOME to result.lowercase(),
            ),
        )
    }

    override fun logSearch(mode: String) {
        val searchMode = halSearchMode(mode) ?: return
        events.track(EVENT_SEARCH, dims = mapOf(DIM_MODE to searchMode))
    }

    override fun logVipUpgrade(plan: String) {
        events.track(EVENT_VIP_UPGRADE, dims = mapOf(DIM_PLAN to plan.lowercase()))
    }

    override fun logVipUpsell(source: String) {
        events.track(EVENT_VIP_UPSELL, dims = mapOf(DIM_SOURCE to source))
    }

    override fun logShare(
        source: String,
        type: String?,
    ) {
        val dims = buildMap {
            put(DIM_SOURCE, source)
            type?.let { put(DIM_TYPE, it.lowercase()) }
        }
        events.track(EVENT_SHARE, dims = dims)
    }

    override fun logFollow(action: String) {
        events.track(EVENT_FOLLOW, dims = mapOf(DIM_ACTION to action))
    }

    override fun logBlock(blocked: Boolean) {
        events.track(EVENT_BLOCK, dims = mapOf(DIM_ACTION to if (blocked) "block" else "unblock"))
    }

    override fun logTheme(theme: String) {
        events.track(EVENT_THEME, dims = mapOf(DIM_THEME to theme.lowercase()))
    }

    override fun logCoverImage(mediaType: String) {
        events.track(EVENT_COVER_IMAGE, dims = mapOf(DIM_TYPE to mediaType.lowercase()))
    }

    override fun logCalendarPeriod(action: String) {
        events.track(EVENT_CALENDAR_PERIOD, dims = mapOf(DIM_ACTION to action))
    }

    override fun logFilter(
        id: String,
        set: Boolean,
        mode: String,
    ) {
        events.track(
            EVENT_FILTER,
            dims = mapOf(
                DIM_ID to id,
                DIM_ACTION to if (set) "set" else "reset",
                DIM_MODE to mode.lowercase(),
            ),
        )
    }
}
