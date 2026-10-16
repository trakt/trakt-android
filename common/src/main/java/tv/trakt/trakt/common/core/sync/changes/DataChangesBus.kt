package tv.trakt.trakt.common.core.sync.changes

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter

internal class DataChangesBus : DataChanges {
    private val changes = MutableSharedFlow<DataChange>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun notify(
        vararg kinds: DataChange.Kind,
        origin: ChangeOrigin?,
        targetId: Int?,
    ) {
        changes.tryEmit(
            DataChange(
                kinds = kinds.toSet(),
                origin = origin,
                targetId = targetId,
            ),
        )
    }

    override fun observe(
        vararg kinds: DataChange.Kind,
        exclude: ChangeOrigin?,
    ): Flow<DataChange> {
        val wanted = kinds.toSet()
        return changes.filter { change ->
            if (exclude != null && change.origin == exclude) {
                return@filter false
            }
            change.kinds.any { it in wanted }
        }
    }
}
