package tv.trakt.trakt.common.auth.session.helpers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import tv.trakt.trakt.common.model.User

fun Flow<User?>.distinctUntilSessionChanged(): Flow<User?> {
    return distinctUntilChanged { old, new ->
        old?.ids?.trakt == new?.ids?.trakt && old?.isAnyVip == new?.isAnyVip
    }
}
