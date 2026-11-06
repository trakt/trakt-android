package tv.trakt.trakt.core.settings.data

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.model.User
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
internal fun SessionManager.observeSettingsProfile(): Flow<User?> {
    return flow {
        emit(getProfile())
        emitAll(
            observeProfile()
                .distinctUntilChanged()
                .debounce(200.milliseconds),
        )
    }
}
