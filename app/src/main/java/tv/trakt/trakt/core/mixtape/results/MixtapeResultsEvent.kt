package tv.trakt.trakt.core.mixtape.results

import tv.trakt.trakt.core.mixtape.model.MixtapeTarget

internal sealed interface MixtapeResultsEvent {
    data class Moved(
        val count: Int,
        val target: MixtapeTarget,
    ) : MixtapeResultsEvent

    data object MoveFailed : MixtapeResultsEvent

    data class KeepFailed(
        val title: String,
    ) : MixtapeResultsEvent
}
