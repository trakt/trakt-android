package tv.trakt.trakt.common.helpers

enum class LoadingState {
    IDLE,
    LOADING,
    DONE,
    ;

    val isIdle: Boolean
        get() = this == IDLE

    val isLoading: Boolean
        get() = this == LOADING

    val isDone: Boolean
        get() = this == DONE
}
