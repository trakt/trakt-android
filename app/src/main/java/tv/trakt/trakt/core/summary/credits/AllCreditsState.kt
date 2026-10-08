package tv.trakt.trakt.core.summary.credits

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.summary.credits.model.CreditsMode
import tv.trakt.trakt.core.summary.credits.model.MediaCredits

@Immutable
internal data class AllCreditsState(
    val mediaTitle: String? = null,
    val backgroundUrl: String? = null,
    val credits: MediaCredits? = null,
    val mode: CreditsMode = CreditsMode.Main,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
