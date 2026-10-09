package tv.trakt.trakt.core.applinks

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory signal bumped each time a share arrival is credited, so share-dependent views can refresh.
 */
internal class ShareArrivalEvents {
    private val creditedFlow = MutableStateFlow(0)
    val credited: StateFlow<Int> = creditedFlow.asStateFlow()

    fun notifyCredited() {
        creditedFlow.update { it + 1 }
    }
}
