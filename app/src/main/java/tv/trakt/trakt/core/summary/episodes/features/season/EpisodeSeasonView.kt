@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.episodes.features.season

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.core.summary.episodes.features.season.ui.EpisodeSeasonList
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun EpisodeSeasonView(
    viewModel: EpisodeSeasonViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onEpisodeClick: (episode: Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snack = LocalSnackbarState.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var confirmRemoveSheet by remember { mutableStateOf<EpisodeItem?>(null) }

    EpisodeSeasonContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onEpisodeClick = { onEpisodeClick(it.episode) },
        onCheckEpisodeClick = {
            viewModel.addToWatched(it.episode)
        },
        onRemoveEpisodeClick = {
            confirmRemoveSheet = it
        },
    )

    ConfirmationSheet(
        active = confirmRemoveSheet != null,
        onYes = {
            confirmRemoveSheet?.let {
                viewModel.removeFromWatched(it.episode)
                confirmRemoveSheet = null
            }
        },
        onNo = { confirmRemoveSheet = null },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_from_watched,
            "${confirmRemoveSheet?.episode?.title}",
        ),
    )

    LaunchedEffect(state.info) {
        if (state.info == null) {
            return@LaunchedEffect
        }

        haptic.performHapticFeedback(Confirm)
        with(scope) {
            val job = launch {
                state.info?.get(context)?.let {
                    snack.showSnackbar(it)
                }
            }
            delay(SNACK_DURATION_SHORT)
            job.cancel()
        }

        viewModel.clearInfo()
    }
}

@Composable
private fun EpisodeSeasonContent(
    state: EpisodeSeasonState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onCheckEpisodeClick: ((EpisodeItem) -> Unit)? = null,
    onRemoveEpisodeClick: ((EpisodeItem) -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        val headerText = state.seasonNumber?.let {
            when {
                it == 0 -> stringResource(R.string.text_season_specials)
                else -> stringResource(R.string.text_season_number, it)
            }
        }

        TraktHeader(
            title = headerText ?: stringResource(R.string.list_title_seasons),
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding),
        )

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(300),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ContentLoading(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
                    if (state.episodes.isEmpty()) {
                        ContentEmpty(
                            contentPadding = contentPadding,
                        )
                    } else {
                        EpisodeSeasonList(
                            show = state.show,
                            episodes = state.episodes,
                            contentPadding = contentPadding,
                            onEpisodeClick = onEpisodeClick ?: {},
                            onCheckClick = onCheckEpisodeClick ?: {},
                            onRemoveClick = onRemoveEpisodeClick ?: {},
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentLoading(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (visible) 1F else 0F),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
    ) {
        items(count = 3) {
            EpisodeSkeletonCard()
        }
    }
}

@Composable
private fun ContentEmpty(contentPadding: PaddingValues) {
    Text(
        text = stringResource(R.string.list_placeholder_empty),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
        modifier = Modifier.padding(contentPadding),
    )
}
