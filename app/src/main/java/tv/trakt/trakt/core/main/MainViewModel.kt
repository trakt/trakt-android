package tv.trakt.trakt.core.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.user.usecase.progress.LoadUserProgressUseCase
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

@OptIn(FlowPreview::class)
internal class MainViewModel(
    private val sessionManager: SessionManager,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
) : ViewModel() {
    private val initialState = MainState()
    private val userState = MutableStateFlow(initialState.user)

    private var lastLoadTime: Instant? = null

    init {
        observeUser()
    }

    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(250)
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
    }

    fun loadData() {
        if (lastLoadTime != null && nowUtcInstant().minus(5, MINUTES) < lastLoadTime) {
            Timber.d("Skipping...")
            return
        }

        viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    return@launch
                }
                loadUserProgressUseCase.loadAllProgress()
                lastLoadTime = nowUtcInstant()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    val state: StateFlow<MainState> = combine(
        userState,
    ) { state ->
        MainState(
            user = state[0],
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
