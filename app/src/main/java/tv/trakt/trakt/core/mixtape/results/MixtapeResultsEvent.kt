package tv.trakt.trakt.core.mixtape.results

import tv.trakt.trakt.core.mixtape.model.MixtapeMove

internal sealed interface MixtapeResultsEvent {
    data class Moved(
        val count: Int,
        val move: MixtapeMove,
    ) : MixtapeResultsEvent

    data object MoveFailed : MixtapeResultsEvent

    data class KeepFailed(
        val title: String,
    ) : MixtapeResultsEvent
}
