package tv.trakt.trakt.app.core.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.remoteconfig.remoteConfig
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import timber.log.Timber
import tv.trakt.trakt.app.core.auth.AuthState.LoadingState.LOADING
import tv.trakt.trakt.app.core.auth.AuthState.LoadingState.REJECTED
import tv.trakt.trakt.app.core.auth.AuthState.LoadingState.SUCCESS
import tv.trakt.trakt.app.core.auth.model.AuthDeviceTokenCode.PENDING
import tv.trakt.trakt.app.core.auth.model.AuthDeviceTokenState.Failure
import tv.trakt.trakt.app.core.auth.model.AuthDeviceTokenState.Success
import tv.trakt.trakt.app.core.auth.usecases.GetDeviceCodeUseCase
import tv.trakt.trakt.app.core.auth.usecases.GetDeviceTokenUseCase
import tv.trakt.trakt.app.core.auth.usecases.LoadUserProfileUseCase
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import kotlin.time.Duration

internal class AuthViewModel(
    private val getDeviceCodeUseCase: GetDeviceCodeUseCase,
    private val getDeviceTokenUseCase: GetDeviceTokenUseCase,
    private val loadUserProfileUseCase: LoadUserProfileUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = AuthState()

    private val loadingState = MutableStateFlow(initialState.loadingState)
    private val deviceCodeState = MutableStateFlow(initialState.authDeviceCode)
    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadBackground()
        loadData()
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }
                val authData = getDeviceCodeUseCase.getDeviceCode()

                deviceCodeState.update { authData }

                loadingState.update { null }

                delay(authData.interval)
                pollDeviceToken(
                    deviceCode = authData.deviceCode,
                    delayDuration = authData.interval,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Error loading data")
                }
            } finally {
                loadingState.update { null }
            }
        }
    }

    private fun pollDeviceToken(
        deviceCode: String,
        delayDuration: Duration,
    ) {
        if (nowUtc().isAfter(deviceCodeState.value?.expiresAt)) {
            deviceCodeState.update { null }
            loadingState.update { REJECTED }
            return
        }

        viewModelScope.launch {
            try {
                when (val state = getDeviceTokenUseCase.getDeviceToken(deviceCode)) {
                    is Success -> {
                        loadUserProfileUseCase.loadUserProfile()
                        deviceCodeState.update { null }
                        loadingState.update { SUCCESS }
                        Timber.i("Device token received successfully")
                    }

                    is Failure -> when (state.code) {
                        PENDING -> {
                            delay(delayDuration)
                            pollDeviceToken(
                                deviceCode = deviceCode,
                                delayDuration = delayDuration,
                            )
                        }

                        else -> {
                            deviceCodeState.update { null }
                            loadingState.update { REJECTED }
                        }
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    launch { sessionManager.clear() }
                    errorState.update { error }
                    logErrorIfNeeded(error)
                }
            }
        }
    }

    private fun logErrorIfNeeded(error: Exception) {
        val crashlytics = Firebase.crashlytics

        when (error) {
            is ClientRequestException -> {
                val code = error.response.status.value
                if (code in 500..599 || code in 400..499) {
                    crashlytics.recordException(error)
                }
            }
            is SerializationException -> {
                crashlytics.recordException(error)
            }
        }

        Timber.w(error, "Error polling device token")
    }

    val state: StateFlow<AuthState> = combine(
        loadingState,
        deviceCodeState,
        backgroundState,
        errorState,
    ) { s1, s2, s3, s4 ->
        AuthState(
            loadingState = s1,
            authDeviceCode = s2,
            backgroundUrl = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
