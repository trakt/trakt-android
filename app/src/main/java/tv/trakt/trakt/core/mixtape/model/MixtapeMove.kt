package tv.trakt.trakt.core.mixtape.model

import tv.trakt.trakt.common.model.mixtape.MixtapeItem

internal data class MixtapeMove(
    val from: MixtapeTarget,
    val to: MixtapeTarget,
    val items: List<MixtapeItem>,
    val added: List<MixtapeItem>,
)
