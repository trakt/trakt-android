@file:OptIn(FlowPreview::class)

package tv.trakt.trakt.core.settings.features.younify

import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.settings.features.younify.model.LinkStatus
import tv.trakt.trakt.core.settings.features.younify.model.YounifyDetails
import tv.trakt.trakt.core.settings.features.younify.model.YounifyServices
import tv.trakt.trakt.core.settings.features.younify.model.linkStatus
import tv.trakt.trakt.core.settings.features.younify.usecases.GetYounifyDetailsUseCase
import tv.trakt.trakt.core.settings.features.younify.usecases.RefreshYounifyTokensUseCase
import tv.younify.sdk.connect.Connect
import tv.younify.sdk.connect.ConnectOptions
import tv.younify.sdk.connect.LogLevel
import tv.younify.sdk.connect.LogListener
import tv.younify.sdk.connect.RenewTokensCallback
import tv.younify.sdk.connect.StreamingService
import tv.younify.sdk.connect.TokenHandler
import tv.younify.sdk.connect.UserConsentRequiredException

internal class YounifyViewModel(
    private val younify: Connect,
    private val sessionManager: SessionManager,
    private val getYounifyDetailsUseCase: GetYounifyDetailsUseCase,
    private val refreshYounifyTokensUseCase: RefreshYounifyTokensUseCase,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = YounifyState()

    private val userState = MutableStateFlow(initialState.user)
    private val servicesState = MutableStateFlow(initialState.younifyServices)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var refreshTokenJob: Job? = null

    init {
        loadUser()
        loadData()

        analytics.logScreenView(
            screenName = "younify",
        )
    }

    private fun loadUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200)
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            loadingState.update { LoadingState.LOADING }

            try {
                userState.update {
                    sessionManager.getProfile()
                }

                if (!sessionManager.isAuthenticated() || userState.value?.isAnyVip != true) {
                    Timber.w("Not authenticated, skipping Younify details load")
                    return@launch
                }

                val younifyDetails = getYounifyDetailsUseCase.getYounifyDetails(
                    generateTokens = false,
                )

                connectYounify(younifyDetails)
                loadYounifyServices(younifyDetails.services)
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
        younify.configure(options)
    }

    private suspend fun loadYounifyServices(knownServices: YounifyServices) {
        val knownServicesIds = knownServices.available.watched.asyncMap { it.id }
        val remoteServices = younify.fetchServices()

        servicesState.update {
            remoteServices
                .filter { it.id in knownServicesIds }
                .sortedBy { it.name.lowercase() }
                .toImmutableList()
        }
    }

    private fun linkService(
        service: StreamingService,
        context: Context,
        registry: ActivityResultRegistry,
        promptSync: Boolean,
    ) {
        viewModelScope.launch {
            try {
                val resultSuccess = younify.linkService(
                    context = context,
                    registry = registry,
                    service = service,
                )

                if (resultSuccess) {
                    Timber.d("Successfully linked service ${service.name}")
                } else {
                    // TODO
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (error is UserConsentRequiredException) {
                        return@rethrowCancellation
                    }
                    errorState.update { error }
                    Timber.recordError(error)
                }
            }
        }
    }

    fun onServiceAction(
        service: StreamingService,
        context: Context,
        registry: ActivityResultRegistry?,
    ) {
        if (registry == null) {
            Timber.e("No activity registry")
            return
        }

        when (service.linkStatus) {
            LinkStatus.LINKED -> TODO()

            LinkStatus.UNLINKED -> linkService(
                service = service,
                promptSync = true,
                context = context,
                registry = registry,
            )

            LinkStatus.BROKEN -> linkService(
                service = service,
                promptSync = false,
                context = context,
                registry = registry,
            )
        }
    }

    override fun onCleared() {
        dataJob?.cancel()
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
        servicesState,
        loadingState,
        errorState,
    ) { state ->
        @Suppress("UNCHECKED_CAST")
        YounifyState(
            user = state[0] as User?,
            younifyServices = state[1] as ImmutableList<StreamingService>?,
            loading = state[2] as LoadingState,
            error = state[3] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
