package tv.trakt.trakt.core.summary.livingposter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.core.summary.livingposter.ui.LivingPosterStage
import tv.trakt.trakt.helpers.extensions.rememberAnimationsDisabled

/** [onReady] reports whether the living poster is loaded and should replace the static poster. */
@Composable
internal fun LivingPosterView(
    viewModel: LivingPosterViewModel,
    onReady: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tiltEnabled: Boolean = true,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val animationsDisabled = rememberAnimationsDisabled()

    LivingPosterContent(
        state = state,
        animationsDisabled = animationsDisabled,
        onReady = onReady,
        tiltEnabled = tiltEnabled,
        modifier = modifier,
    )
}

@Composable
private fun LivingPosterContent(
    state: LivingPosterState,
    animationsDisabled: Boolean,
    onReady: (Boolean) -> Unit,
    tiltEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!state.enabled || animationsDisabled) return
    val poster = state.poster ?: return

    LivingPosterStage(
        poster = poster,
        onReady = onReady,
        tiltEnabled = tiltEnabled,
        modifier = modifier.fillMaxSize(),
    )
}
