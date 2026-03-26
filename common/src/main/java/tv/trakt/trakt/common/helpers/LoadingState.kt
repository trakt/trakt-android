package tv.trakt.trakt.common.helpers

enum class LoadingState {
    Idle,
    Loading,
    Done,
    ;

    val isIdle: Boolean
        get() = this == Idle

    val isLoading: Boolean
        get() = this == Loading

    val isDone: Boolean
        get() = this == Done
}
