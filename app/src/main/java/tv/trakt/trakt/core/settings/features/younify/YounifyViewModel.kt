@file:OptIn(FlowPreview::class)

package tv.trakt.trakt.core.settings.features.younify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.settings.features.younify.data.remote.model.YounifyDetails
import tv.trakt.trakt.core.settings.features.younify.usecases.GetYounifyDetailsUseCase
import tv.trakt.trakt.core.settings.features.younify.usecases.RefreshYounifyTokensUseCase
import tv.younify.sdk.connect.Connect
import tv.younify.sdk.connect.ConnectOptions
import tv.younify.sdk.connect.LogLevel
import tv.younify.sdk.connect.LogListener
import tv.younify.sdk.connect.RenewTokensCallback
import tv.younify.sdk.connect.TokenHandler

internal class YounifyViewModel(
    private val sessionManager: SessionManager,
    private val getYounifyDetailsUseCase: GetYounifyDetailsUseCase,
    private val refreshYounifyTokensUseCase: RefreshYounifyTokensUseCase,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = YounifyState()

    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var refreshTokenJob: Job? = null

    init {
        loadUser()
        loadData()

        analytics.logScreenView(
            screenName = "younify",
        )
    }

    private fun loadUser() {
        viewModelScope.launch {
            userState.update {
                sessionManager.getProfile()
            }
        }

        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
    }

    // TODO Check VIP and message accordingly
    private fun loadData() {
        viewModelScope.launch {
            loadingState.update { LoadingState.LOADING }
            try {
                if (!sessionManager.isAuthenticated()) {
                    Timber.w("Not authenticated, skipping Younify details load")
                    return@launch
                }

                getYounifyDetailsUseCase.getYounifyDetails(
                    generateTokens = false,
                ).apply {
                    Timber.d("Younify details loaded: $this")
                    connectYounify(this)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { LoadingState.DONE }
            }
        }
    }

    private fun connectYounify(details: YounifyDetails) {
        val options = ConnectOptions(
            key = BuildConfig.YOUNIFY_API_KEY,
            accessToken = details.tokens.accessToken,
            refreshToken = details.tokens.refreshToken,
            tokenHandler = younifyTokenHandler,
            logLevel = when {
                BuildConfig.DEBUG -> LogLevel.Trace
                else -> LogLevel.Debug
            },
            logListener = younifyLogListener,
            extra = null,
        )
        Connect.configure(options)
    }

    override fun onCleared() {
        refreshTokenJob?.cancel()
        super.onCleared()
    }

    private val younifyTokenHandler = object : TokenHandler {
        override fun renewTokens(
            expiredAccessToken: String?,
            refreshToken: String?,
            complete: RenewTokensCallback,
        ) {
            refreshTokenJob?.cancel()
            refreshTokenJob = viewModelScope.launch {
                try {
                    refreshYounifyTokensUseCase.refreshTokens()
                        .apply {
                            complete(
                                tokens.accessToken,
                                tokens.refreshToken,
                            )
                        }
                } catch (error: Exception) {
                    error.rethrowCancellation {
                        Timber.recordError(error)
                    }
                }
            }
        }

        override fun renewedTokens(
            newAccessToken: String,
            newRefreshToken: String,
        ) = Unit
    }

    private val younifyLogListener = object : LogListener {
        override fun log(
            level: LogLevel,
            message: String,
        ) {
            Timber.log(level.value, "Younify: $message")
        }
    }

    val state = combine(
        userState,
        loadingState,
        errorState,
    ) { state ->
        YounifyState(
            user = state[0] as User?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
