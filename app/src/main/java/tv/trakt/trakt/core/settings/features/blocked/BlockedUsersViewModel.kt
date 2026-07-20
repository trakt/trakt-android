package tv.trakt.trakt.core.settings.features.blocked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.core.user.usecases.blocking.BlockUserUseCase
import tv.trakt.trakt.common.core.user.usecases.blocking.GetBlockedUsersUseCase
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.resources.R

internal class BlockedUsersViewModel(
    private val getBlockedUsersUseCase: GetBlockedUsersUseCase,
    private val blockUserUseCase: BlockUserUseCase,
) : ViewModel() {
    private val initialState = BlockedUsersState()

    private val usersState = MutableStateFlow(initialState.users)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val infoState = MutableStateFlow(initialState.info)

    init {
        loadBlockedUsers()
    }

    private fun loadBlockedUsers() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }
                val blocked = getBlockedUsersUseCase.getBlockedUsers()
                usersState.update { blocked.keys.toImmutableList() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun unblockUser(user: User) {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }
                blockUserUseCase.unblockUser(user.ids.trakt)
                infoState.update {
                    DynamicStringResource(
                        R.string.text_info_unblocked,
                        listOf(user.displayName),
                    )
                }
                loadBlockedUsers()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    val state = combine(
        usersState,
        loadingState,
        infoState,
    ) { users, loading, info ->
        BlockedUsersState(
            users = users,
            loading = loading,
            info = info,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
