package tv.trakt.trakt.core.lists.sheets.collaborators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.lists.sheets.collaborators.usecases.ManageListCollaboratorsUseCase

internal class LeaveCollaborationViewModel(
    private val sessionManager: SessionManager,
    private val manageCollaboratorsUseCase: ManageListCollaboratorsUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val failedFlow = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
    )
    val failed = failedFlow.asSharedFlow()

    fun leave(list: CustomList) {
        viewModelScope.launch {
            try {
                val user = sessionManager.getProfile() ?: return@launch
                manageCollaboratorsUseCase.removeCollaborator(list.ids.trakt, user)
                analytics.lists.logListCollaborator("leave")
            } catch (error: Exception) {
                error.rethrowCancellation {
                    failedFlow.tryEmit(Unit)
                    Timber.recordError(error)
                }
            }
        }
    }
}
