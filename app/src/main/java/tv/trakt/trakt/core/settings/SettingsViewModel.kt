package tv.trakt.trakt.core.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.settings.data.observeSettingsProfile
import tv.trakt.trakt.core.settings.features.avatar.AvatarCrop
import tv.trakt.trakt.core.settings.usecases.UpdateUserAvatarUseCase
import tv.trakt.trakt.resources.R

@Suppress("UNCHECKED_CAST")
internal class SettingsViewModel(
    private val sessionManager: SessionManager,
    private val updateAvatarUseCase: UpdateUserAvatarUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = SettingsState()

    private val userState = MutableStateFlow(initialState.user)
    private val avatarLoadingState = MutableStateFlow(initialState.avatarLoading)
    private val avatarVersionState = MutableStateFlow(initialState.avatarVersion)

    init {
        loadUser()

        analytics.logScreenView(
            screenName = "settings",
        )
    }

    private fun loadUser() {
        sessionManager.observeSettingsProfile()
            .onEach { user ->
                userState.update { user }
            }
            .launchIn(viewModelScope)
    }

    fun logRedditLink() {
        analytics.logLink(source = "settings", target = "reddit")
    }

    fun uploadAvatar(
        image: Uri,
        crop: AvatarCrop,
    ) {
        updateAvatar(action = "upload-avatar") {
            updateAvatarUseCase.uploadAvatar(
                image = image,
                crop = crop,
            )
        }
    }

    fun removeAvatar() {
        updateAvatar(action = "remove-avatar") {
            updateAvatarUseCase.removeAvatar()
        }
    }

    private fun updateAvatar(
        action: String,
        update: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                avatarLoadingState.update { Loading }
                update()
                avatarVersionState.update { it + 1 }
                analytics.logSettings(action)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error, "Unable to update avatar")
                }
                info.emit(DynamicStringResource(R.string.error_text_failed_update))
            } finally {
                avatarLoadingState.update { Done }
            }
        }
    }

    val info: Flow<StringResource>
        field = MutableSharedFlow<StringResource>(replay = 0)

    val state = combine(
        userState,
        avatarLoadingState,
        avatarVersionState,
    ) { state ->
        SettingsState(
            user = state[0] as User?,
            avatarLoading = state[1] as LoadingState,
            avatarVersion = state[2] as Int,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}
