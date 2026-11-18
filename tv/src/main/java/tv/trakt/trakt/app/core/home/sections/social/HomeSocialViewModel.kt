package tv.trakt.trakt.app.core.home.sections.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.app.core.home.sections.social.usecases.GetSocialActivityUseCase
import tv.trakt.trakt.common.data.local.MediaModeLocalSource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode

internal class HomeSocialViewModel(
    private val getSocialActivityUseCase: GetSocialActivityUseCase,
    private val mediaModeLocalSource: MediaModeLocalSource,
) : ViewModel() {
    private val initialState = HomeSocialState()
    private var mode = MediaMode.Media
    private var dataJob: Job? = null

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        observeMode()
    }

    private fun observeMode() {
        mediaModeLocalSource.observeMode()
            .onEach {
                mode = it
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadingState.update { true }

                itemsState.update {
                    getSocialActivityUseCase.getSocialActivity(HOME_SECTION_LIMIT, mode)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error, "Error loading social activity")
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    val state: StateFlow<HomeSocialState> = combine(
        loadingState,
        itemsState,
        errorState,
    ) { s1, s2, s3 ->
        HomeSocialState(
            isLoading = s1,
            items = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
