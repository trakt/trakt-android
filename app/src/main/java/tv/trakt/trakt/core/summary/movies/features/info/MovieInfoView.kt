package tv.trakt.trakt.core.summary.movies.features.info

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
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.core.summary.ui.DetailsMetaInfo
import tv.trakt.trakt.core.summary.ui.views.info.MetaView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.extensions.isAtLeastLarge
import tv.trakt.trakt.ui.extensions.isAtLeastMedium
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieInfoView(
    viewModel: MovieInfoViewModel,
    modifier: Modifier = Modifier,
    onPersonClick: (person: Person) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MovieInfoView(
        state = state,
        onPersonClick = onPersonClick,
        modifier = modifier,
    )
}

@Composable
private fun MovieInfoView(
    state: MovieInfoState,
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

        state.movie?.let {
            Column(
                verticalArrangement = spacedBy(24.dp),
            ) {
                MetaView(
                    plays = state.movieStats?.plays ?: 0,
                    watchers = state.movieStats?.watchers ?: 0,
                    lists = state.movieStats?.lists ?: 0,
                    favorites = state.movieStats?.favorited ?: 0,
                    loading = !state.loading.isDone,
                    isReleased = it.isReleased,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 23.dp),
                )

                DetailsView(
                    movie = it,
                    movieStudios = state.movieStudios,
                    movieDirectors = state.movieCrew?.directors,
                    movieWriters = state.movieCrew?.writers,
                    onPersonClick = onPersonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun DetailsView(
    movie: Movie,
    movieStudios: ImmutableList<String>?,
    movieDirectors: ImmutableList<Person>?,
    movieWriters: ImmutableList<Person>?,
    onPersonClick: (person: Person) -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowClass = currentWindowAdaptiveInfo().windowSizeClass
    DetailsMetaInfo(
        movie = movie,
        movieStudios = movieStudios,
        movieDirectors = movieDirectors,
        movieWriters = movieWriters,
        onPersonClick = onPersonClick,
        modifier = modifier
            .fillMaxWidth(
                when {
                    windowClass.isAtLeastLarge() -> 0.4F
                    windowClass.isAtLeastMedium() -> 0.66F
                    else -> 1F
                },
            ),
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        MovieInfoView(
            state = MovieInfoState(
                movie = PreviewData.movie1,
                loading = LoadingState.Done,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview2() {
    TraktTheme {
        MovieInfoView(
            state = MovieInfoState(
                movie = PreviewData.movie1,
                loading = LoadingState.Loading,
            ),
        )
    }
}
