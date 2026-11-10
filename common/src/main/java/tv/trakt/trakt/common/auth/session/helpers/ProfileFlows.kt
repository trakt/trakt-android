package tv.trakt.trakt.common.auth.session.helpers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import tv.trakt.trakt.common.model.User

fun Flow<User?>.distinctIgnoringPreferences(): Flow<User?> {
    return distinctUntilChanged { old, new ->
        old?.withoutPreferences() == new?.withoutPreferences()
    }
}

private fun User.withoutPreferences(): User {
    return copy(
        settings = settings?.copy(
            hideSpoilers = false,
            watchOnlyOnce = false,
            ratingPrompts = false,
        ),
    )
}
