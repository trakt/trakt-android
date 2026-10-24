package tv.trakt.trakt.common.networking.helpers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val COLLECTION_LIMIT_MESSAGE = "collection has exceeded"

enum class AccountLock {
    UsageLimit,
    CollectionLimit,
}

class AccountLockMonitor {
    private val lockState = MutableStateFlow<AccountLock?>(null)

    fun observeLock(): StateFlow<AccountLock?> = lockState.asStateFlow()

    fun reportLock(message: String?) {
        val isCollectionLimit = message?.contains(COLLECTION_LIMIT_MESSAGE, ignoreCase = true) == true
        lockState.update {
            if (isCollectionLimit) AccountLock.CollectionLimit else AccountLock.UsageLimit
        }
    }

    fun clear() {
        lockState.update { null }
    }
}
