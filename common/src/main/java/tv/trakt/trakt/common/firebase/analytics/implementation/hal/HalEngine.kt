package tv.trakt.trakt.common.firebase.analytics.implementation.hal

import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.HalEvent
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.HalEventStore

internal const val HAL_USER_TYPE_DIM = "user_type"
private const val HAL_ANONYMOUS_USER_TYPE = "anonymous"

internal interface HalEvents {
    fun track(
        name: String,
        dims: Map<String, String> = emptyMap(),
        metrics: Map<String, Long> = emptyMap(),
        allowAnonymous: Boolean = false,
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
        // Logout cancels all WorkManager work, so events queued before it need a fresh send.
        if (userType == null && store.isNotEmpty()) {
            scheduleSend()
        }
    }

    override fun track(
        name: String,
        dims: Map<String, String>,
        metrics: Map<String, Long>,
        allowAnonymous: Boolean,
    ) {
        val currentUserType = userType
            ?: HAL_ANONYMOUS_USER_TYPE.takeIf { allowAnonymous }
            ?: return
        val event = HalEvent(
            name = name,
            at = clock(),
            dims = dims + (HAL_USER_TYPE_DIM to currentUserType),
            metrics = metrics,
        )

        store.add(event)
        scheduleSend()
    }
}
