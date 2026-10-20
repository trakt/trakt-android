package tv.trakt.trakt.common.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.common.model.reactions.UserMediaReaction
import java.time.Instant

class UserMediaReactionsLocalSource {
    private val mutex = Mutex()

    private val storage = mutableMapOf<MediaReactionsTarget, List<UserMediaReaction>>()
    private val updatedAt = MutableSharedFlow<Instant?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    suspend fun setReactions(
        target: MediaReactionsTarget,
        reactions: List<UserMediaReaction>,
        notify: Boolean = false,
    ) {
        mutex.withLock {
            storage[target] = reactions.toList()

            if (notify) {
                updatedAt.tryEmit(nowUtcInstant())
            }
        }
    }

    suspend fun getReactions(target: MediaReactionsTarget): List<UserMediaReaction> {
        return mutex.withLock {
            storage[target].orEmpty()
        }
    }

    suspend fun isLoaded(target: MediaReactionsTarget): Boolean {
        return mutex.withLock {
            storage.containsKey(target)
        }
    }

    fun observeUpdates(): Flow<Instant?> {
        return updatedAt
    }

    fun clear() {
        storage.clear()
        updatedAt.tryEmit(null)
    }
}
