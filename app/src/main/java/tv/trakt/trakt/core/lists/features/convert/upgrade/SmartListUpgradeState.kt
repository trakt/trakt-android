package tv.trakt.trakt.core.lists.features.convert.upgrade

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListDraft

@Immutable
internal data class SmartListUpgradeState(
    val visible: Boolean = false,
    val draft: SmartListDraft? = null,
    val loading: LoadingState = LoadingState.Idle,
    val navigateSmartList: SmartList? = null,
)
