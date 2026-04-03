package tv.trakt.trakt.core.settings.features.cover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.settings.usecases.UpdateUserSettingsUseCase

internal class CoverImageViewModel(
    mediaId: TraktId,
    mediaTitle: String,
    mediaType: MediaType,
    mediaImage: String?,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = CoverImageState(
        mediaId = mediaId,
        mediaTitle = mediaTitle,
        mediaType = mediaType,
        mediaImage = mediaImage,
    )

    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun setCoverImage() {
        viewModelScope.launch {
            try {
                errorState.update { null }
                loadingState.update { LoadingState.Loading }

                updateUserSettingsUseCase.updateCoverImage(
                    mediaId = initialState.mediaId,
                    mediaType = initialState.mediaType,
                )

                loadingState.update { LoadingState.Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    loadingState.update { LoadingState.Idle }
                    Timber.recordError(error)
                }
            }
        }
    }

    val state = combine(
        userState,
        loadingState,
        errorState,
    ) { s1, s2, s3 ->
        CoverImageState(
            mediaId = mediaId,
            mediaTitle = mediaTitle,
            mediaType = mediaType,
            mediaImage = mediaImage,
            user = s1,
            loading = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
