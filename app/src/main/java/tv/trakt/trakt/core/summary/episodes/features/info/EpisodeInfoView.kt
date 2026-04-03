package tv.trakt.trakt.core.summary.episodes.features.info

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.core.summary.ui.DetailsMetaInfo
import tv.trakt.trakt.core.summary.ui.views.info.MetaView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.extensions.isAtLeastLarge
import tv.trakt.trakt.ui.extensions.isAtLeastMedium
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun EpisodeInfoView(
    viewModel: EpisodeInfoViewModel,
    modifier: Modifier = Modifier,
    onPersonClick: (person: Person) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EpisodeInfoView(
        state = state,
        onPersonClick = onPersonClick,
        modifier = modifier,
    )
}

@Composable
private fun EpisodeInfoView(
    state: EpisodeInfoState,
    modifier: Modifier = Modifier,
    onPersonClick: (person: Person) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(20.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.header_details),
            subtitle = null,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        state.episode?.let { episode ->
            Column(
                verticalArrangement = spacedBy(24.dp),
            ) {
                MetaView(
                    plays = state.episodeStats?.plays ?: 0,
                    watchers = state.episodeStats?.watchers ?: 0,
                    lists = state.episodeStats?.lists ?: 0,
                    favorites = null,
                    loading = !state.loading.isDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 23.dp),
                )

                val windowClass = currentWindowAdaptiveInfo().windowSizeClass
                DetailsMetaInfo(
                    episode = episode,
                    episodeDirectors = state.episodeCrew?.directors,
                    episodeWriters = state.episodeCrew?.writers,
                    onPersonClick = onPersonClick,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(
                            when {
                                windowClass.isAtLeastLarge() -> 0.4F
                                windowClass.isAtLeastMedium() -> 0.66F
                                else -> 1F
                            },
                        ),
                )
            }
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        EpisodeInfoView(
            state = EpisodeInfoState(
                show = PreviewData.show1,
                episode = PreviewData.episode1,
                loading = LoadingState.Done,
            ),
        )
    }
}
