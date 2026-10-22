package tv.trakt.trakt.core.main.ui.locked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import tv.trakt.trakt.common.networking.helpers.AccountLock
import tv.trakt.trakt.common.networking.helpers.AccountLockMonitor

@Suppress("UNCHECKED_CAST")
internal class LockedAccountViewModel(
    private val accountLockMonitor: AccountLockMonitor,
) : ViewModel() {
    private val initialState = LockedAccountState()

    fun dismiss() {
        accountLockMonitor.clear()
    }

    val state = combine(
        accountLockMonitor.observeLock(),
    ) { state ->
        LockedAccountState(
            lock = state[0] as AccountLock?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
