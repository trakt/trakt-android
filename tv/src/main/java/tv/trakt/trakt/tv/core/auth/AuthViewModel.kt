package tv.trakt.trakt.tv.core.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.tv.auth.session.SessionManager
import tv.trakt.trakt.tv.core.auth.AuthState.LoadingState.LOADING
import tv.trakt.trakt.tv.core.auth.AuthState.LoadingState.REJECTED
import tv.trakt.trakt.tv.core.auth.AuthState.LoadingState.SUCCESS
import tv.trakt.trakt.tv.core.auth.model.AuthDeviceTokenCode.PENDING
import tv.trakt.trakt.tv.core.auth.model.AuthDeviceTokenState.Failure
import tv.trakt.trakt.tv.core.auth.model.AuthDeviceTokenState.Success
import tv.trakt.trakt.tv.core.auth.usecases.GetDeviceCodeUseCase
import tv.trakt.trakt.tv.core.auth.usecases.GetDeviceTokenUseCase
import tv.trakt.trakt.tv.core.auth.usecases.LoadUserProfileUseCase
import tv.trakt.trakt.tv.helpers.extensions.nowUtc
import tv.trakt.trakt.tv.helpers.extensions.rethrowCancellation
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
        val configUrl = Firebase.remoteConfig.getString("background_image_url")
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
                    Log.e("AuthViewModel", "Error loading data", error)
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
                        Log.i("AuthViewModel", "Device token received successfully")
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
                    Log.e("AuthViewModel", "Error polling device token", error)
                }
            }
        }
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
