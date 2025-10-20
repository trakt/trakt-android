package tv.trakt.trakt.core.user.data.local.reactions

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.reactions.Reaction
import java.time.Instant

internal class UserReactionsStorage : UserReactionsLocalDataSource {
    private val mutex = Mutex()

    private var storage: MutableMap<Int, Reaction?>? = null
    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private fun ensureInitialized() {
        if (storage == null) {
            storage = mutableMapOf()
        }
    }

    override suspend fun setReactions(
        reactions: Map<Int, Reaction?>,
        notify: Boolean,
    ) {
        mutex.withLock {
            ensureInitialized()
            storage?.let { storage ->
                storage.clear()
                storage.putAll(reactions)
            }

            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    override suspend fun getReactions(): Map<Int, Reaction?> {
        return mutex.withLock {
            storage?.toMap() ?: emptyMap()
        }
    }

    override suspend fun isLoaded(): Boolean {
        return mutex.withLock {
            storage != null
        }
    }

    override fun observeUpdates(): Flow<Instant?> {
        return updatedAt
    }

    override fun clear() {
        storage?.clear()
        storage = null
        updatedAt.tryEmit(null)
    }
}
