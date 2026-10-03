package tv.trakt.trakt.common.core.user.data.local.reactions

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.common.model.reactions.UserMediaReaction
import java.time.Instant

class UserMediaReactionsStorage : UserMediaReactionsLocalDataSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<MediaReactionsTarget, List<UserMediaReaction>>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun setReactions(
        target: MediaReactionsTarget,
        reactions: List<UserMediaReaction>,
        notify: Boolean,
    ) {
        mutex.withLock {
            storage[target] = reactions.toList()

            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    override suspend fun getReactions(target: MediaReactionsTarget): List<UserMediaReaction> {
        return mutex.withLock {
            storage[target].orEmpty()
        }
    }

    override suspend fun isLoaded(target: MediaReactionsTarget): Boolean {
        return mutex.withLock {
            storage.containsKey(target)
        }
    }

    override fun observeUpdates(): Flow<Instant?> {
        return updatedAt
    }

    override fun clear() {
        storage.clear()
        updatedAt.tryEmit(null)
    }
}
