package tv.trakt.trakt.common.firebase.analytics.implementation.hal

import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.HalEvent
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.HalEventStore

internal const val HAL_USER_TYPE_DIM = "user_type"

internal interface HalEvents {
    fun track(
        name: String,
        dims: Map<String, String> = emptyMap(),
        metrics: Map<String, Int> = emptyMap(),
    )

    fun setUserType(userType: String?)
}

internal class HalEngine(
    private val store: HalEventStore,
    private val scheduleSend: () -> Unit,
    private val clock: () -> Long = System::currentTimeMillis,
) : HalEvents {
    @Volatile
    private var userType: String? = null

    override fun setUserType(userType: String?) {
        this.userType = userType
    }

    override fun track(
        name: String,
        dims: Map<String, String>,
        metrics: Map<String, Int>,
    ) {
        val currentUserType = userType
        val event = HalEvent(
            name = name,
            at = clock(),
            dims = if (currentUserType == null) dims else dims + (HAL_USER_TYPE_DIM to currentUserType),
            metrics = metrics,
        )

        store.add(event)
        scheduleSend()
    }
}
