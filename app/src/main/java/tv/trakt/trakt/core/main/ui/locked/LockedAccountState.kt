package tv.trakt.trakt.core.main.ui.locked

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.networking.helpers.AccountLock

@Immutable
internal data class LockedAccountState(
    val lock: AccountLock? = null,
)
