package tv.trakt.trakt.tv.auth.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.tv.auth.session.DefaultSessionManager
import tv.trakt.trakt.tv.auth.session.SessionManager

private const val SESSION_PREFERENCES = "session_preferences"

internal val tvAuthModule = module {

    single<SessionManager> {
        DefaultSessionManager(
            tokenProvider = get<TokenProvider>(),
            dataStore = get(named(SESSION_PREFERENCES)),
        )
    }
}
