package tv.trakt.trakt.common.helpers.lifecycle

import kotlinx.coroutines.flow.Flow

interface AppLifecycleProvider {
    fun notify(source: State)

    fun observeState(state: State): Flow<State>

    enum class State {
        FOREGROUND,
        BACKGROUND,
    }
}
