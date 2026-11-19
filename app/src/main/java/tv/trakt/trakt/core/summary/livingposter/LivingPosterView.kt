package tv.trakt.trakt.core.summary.livingposter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.core.summary.livingposter.ui.LivingPosterStage
import tv.trakt.trakt.helpers.extensions.rememberAnimationsDisabled

@Composable
internal fun LivingPosterView(
    viewModel: LivingPosterViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val animationsDisabled = rememberAnimationsDisabled()

    LivingPosterContent(
        state = state,
        animationsDisabled = animationsDisabled,
        modifier = modifier,
    )
}

@Composable
private fun LivingPosterContent(
    state: LivingPosterState,
    animationsDisabled: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!state.enabled || animationsDisabled) return
    val poster = state.poster ?: return

    LivingPosterStage(
        poster = poster,
        modifier = modifier,
    )
}
