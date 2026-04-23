package tv.trakt.trakt.core.ratings.rateprompt.ui

import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState
import java.time.Instant

internal data class RatePromptUiState(
    val ratePrompt: RatePromptState? = null,
    val rating: Int? = null,
    val favorite: Boolean = false,
    val dismissing: Instant? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
