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
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.settings.features.younify.model.LinkStatus
import tv.trakt.trakt.core.settings.features.younify.model.YounifyDetails
import tv.trakt.trakt.core.settings.features.younify.model.YounifyServices
import tv.trakt.trakt.core.settings.features.younify.model.linkStatus
import tv.trakt.trakt.core.settings.features.younify.usecases.GetYounifyDetailsUseCase
import tv.trakt.trakt.core.settings.features.younify.usecases.RefreshYounifyDataUseCase
import tv.trakt.trakt.core.settings.features.younify.usecases.RefreshYounifyTokensUseCase
import tv.trakt.trakt.core.settings.features.younify.usecases.UnlinkYounifyServiceUseCase
import tv.trakt.trakt.resources.R
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
    private val refreshYounifyDataUseCase: RefreshYounifyDataUseCase,
    private val unlinkYounifyServiceUseCase: UnlinkYounifyServiceUseCase,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = YounifyState()

    private val userState = MutableStateFlow(initialState.user)
    private val servicesState = MutableStateFlow(initialState.younifyServices)
    private val syncDataPromptState = MutableStateFlow(initialState.syncDataPrompt)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var refreshTokenJob: Job? = null
    private var refreshDataJob: Job? = null

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

    private fun loadData(info: StringResource? = null) {
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

                info?.let { info ->
                    infoState.update { info }
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
        younify.configure(options)
    }

    private suspend fun loadYounifyServices(knownServices: YounifyServices) {
        val knownServicesIds = knownServices.available.watched.asyncMap { it.id }
        val remoteServices = younify.fetchServices()

        servicesState.update {
            remoteServices
                .filter { it.id in knownServicesIds }
                .sortedWith(
                    compareByDescending<StreamingService> {
                        it.linkStatus == LinkStatus.LINKED
                    }.thenBy {
                        it.name.lowercase()
                    },
                )
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
            loadingState.update { LoadingState.LOADING }
            try {
                val resultSuccess = younify.linkService(
                    context = context,
                    registry = registry,
                    service = service,
                )

                if (resultSuccess) {
                    Timber.d("Successfully linked service ${service.name}")

                    if (promptSync) {
                        syncDataPromptState.update { service.id }
                    } else {
                        notifyYounifyRefresh(
                            serviceId = service.id,
                            syncData = false,
                            info = DynamicStringResource(R.string.text_info_younify_linked),
                        )
                    }
                } else {
                    loadingState.update { LoadingState.DONE }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    loadingState.update { LoadingState.DONE }

                    if (error is UserConsentRequiredException) {
                        return@rethrowCancellation
                    }

                    errorState.update { error }
                    Timber.recordError(error)
                }
            }
        }
    }

    fun notifyYounifyRefresh(
        serviceId: String,
        syncData: Boolean,
        info: StringResource?,
    ) {
        syncDataPromptState.update { null }

        refreshDataJob?.cancel()
        refreshDataJob = viewModelScope.launch {
            try {
                refreshYounifyDataUseCase.refresh(
                    serviceId = serviceId,
                    skipSync = !syncData,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }

        loadData(info = info)
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

            LinkStatus.LINKED -> Unit
        }
    }

    fun onServiceEdit(
        service: StreamingService,
        context: Context,
        registry: ActivityResultRegistry?,
    ) {
        if (registry == null) {
            Timber.e("No activity registry")
            return
        }

        viewModelScope.launch {
            try {
                loadingState.update { LoadingState.LOADING }
                younify.manageLinkedService(
                    service = service,
                    context = context,
                    registry = registry,
                )
                notifyYounifyRefresh(
                    serviceId = service.id,
                    syncData = false,
                    info = null,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    loadingState.update { LoadingState.DONE }
                    errorState.update { error }
                    Timber.recordError(error)
                }
            }
        }
    }

    fun onServiceUnlink(service: StreamingService) {
        viewModelScope.launch {
            try {
                loadingState.update { LoadingState.LOADING }

                unlinkYounifyServiceUseCase.unlinkService(service.id)

                loadData(
                    info = DynamicStringResource(R.string.text_info_younify_unlinked),
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    loadingState.update { LoadingState.LOADING }
                    errorState.update { error }
                    Timber.recordError(error)
                }
            }
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    override fun onCleared() {
        dataJob?.cancel()
        refreshTokenJob?.cancel()
        refreshDataJob?.cancel()
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
        syncDataPromptState,
        loadingState,
        infoState,
        errorState,
    ) { state ->
        @Suppress("UNCHECKED_CAST")
        YounifyState(
            user = state[0] as User?,
            younifyServices = state[1] as ImmutableList<StreamingService>?,
            syncDataPrompt = state[2] as String?,
            loading = state[3] as LoadingState,
            info = state[4] as StringResource?,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
