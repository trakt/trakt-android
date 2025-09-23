package tv.trakt.trakt.core.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.user.usecase.progress.LoadUserProgressUseCase
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

internal class MainViewModel(
    private val sessionManager: SessionManager,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
) : ViewModel() {
    private var lastLoadTime: Instant? = null

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
                loadUserProgressUseCase.loadMoviesProgress()
                lastLoadTime = nowUtcInstant()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }
}
