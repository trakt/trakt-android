package tv.trakt.trakt.core.search.features.game

import androidx.lifecycle.ViewModel
import tv.trakt.trakt.common.firebase.analytics.Analytics

internal class GameViewModel(
    analytics: Analytics,
) : ViewModel() {
    init {
        analytics.logScreenView(
            screenName = "game_rated_higher",
        )
    }
}
