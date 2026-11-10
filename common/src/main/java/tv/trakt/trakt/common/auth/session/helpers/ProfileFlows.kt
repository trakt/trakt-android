package tv.trakt.trakt.common.auth.session.helpers

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.model.User
import kotlin.time.Duration.Companion.milliseconds

fun Flow<User?>.distinctUntilSessionChanged(): Flow<User?> {
    return distinctUntilChanged { old, new ->
        old?.ids?.trakt == new?.ids?.trakt && old?.isAnyVip == new?.isAnyVip
    }
}

@OptIn(FlowPreview::class)
fun SessionManager.observeSessionChanges(): Flow<User?> {
    return observeProfile()
        .distinctUntilSessionChanged()
        .drop(1)
        .debounce(200.milliseconds)
}
