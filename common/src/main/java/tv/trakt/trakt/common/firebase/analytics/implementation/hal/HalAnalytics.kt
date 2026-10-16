package tv.trakt.trakt.common.firebase.analytics.implementation.hal

import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_MODE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_OUTCOME
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_PLAN
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_ROUTE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_SOURCE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_TYPE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_DISCOVER_MODE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_PAGE_VIEW
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_SEARCH
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_SHARE_ARRIVAL
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_VIP_UPGRADE

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
}
