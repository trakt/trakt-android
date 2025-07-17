package tv.trakt.trakt.tv.core.home.sections.movies.comingsoon

import InfoChip
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.tv.R
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.tv.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.tv.common.ui.mediacards.HorizontalMediaSkeletonCard
import tv.trakt.trakt.tv.core.home.sections.movies.availablenow.model.WatchlistMovie
import tv.trakt.trakt.tv.core.movies.model.Movie
import tv.trakt.trakt.tv.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.tv.helpers.extensions.relativeDateString
import tv.trakt.trakt.tv.ui.theme.TraktTheme

@Composable
internal fun HomeComingSoonView(
    modifier: Modifier = Modifier,
    viewModel: HomeComingSoonViewModel = koinViewModel(),
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (Movie) -> Unit = {},
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(ON_CREATE) {
        viewModel.updateData()
    }

    HomeComingSoonContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onFocused = onFocused,
        onNavigateToMovie = onNavigateToMovie,
    )
}

@Composable
internal fun HomeComingSoonContent(
    state: HomeComingSoonState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (Movie) -> Unit = {},
    onNavigateToMovie: (TraktId) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.header_movies_coming_soon),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(headerPadding),
        )

        when {
            state.isLoading -> {
                ContentLoadingList(
                    contentPadding = contentPadding,
                )
            }

            state.movies?.isEmpty() == true -> {
                Text(
                    text = stringResource(R.string.info_generic_empty_list),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier
                        .padding(headerPadding),
                )
            }

            else -> {
                ContentList(
                    listItems = { state.movies ?: emptyList<WatchlistMovie>().toImmutableList() },
                    onFocused = onFocused,
                    onClick = { onNavigateToMovie(it.ids.trakt) },
                    contentPadding = contentPadding,
                )
            }
        }
    }
}

@Composable
private fun ContentList(
    listItems: () -> ImmutableList<WatchlistMovie>,
    onFocused: (Movie) -> Unit,
    onClick: (Movie) -> Unit,
    contentPadding: PaddingValues,
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(
            items = listItems(),
            key = { it.movie.ids.trakt.value },
        ) { item ->
            val dateString = remember(item.movie.released) {
                item.movie.released?.relativeDateString()
            }

            HorizontalMediaCard(
                title = item.movie.title,
                containerImageUrl = item.movie.images?.getFanartUrl(),
                contentImageUrl = item.movie.images?.getLogoUrl(),
                paletteColor = item.movie.colors?.colors?.second,
                onClick = { onClick(item.movie) },
                footerContent = {
                    dateString?.let {
                        InfoChip(
                            text = it,
                            iconPainter = painterResource(R.drawable.ic_calendar_upcoming),
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                },
                modifier = Modifier
                    .onFocusChanged {
                        if (it.isFocused) {
                            onFocused(item.movie)
                        }
                    },
            )
        }

        emptyFocusListItems()
    }
}

@Composable
private fun ContentLoadingList(contentPadding: PaddingValues) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(count = 10) {
            HorizontalMediaSkeletonCard()
        }
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        HomeComingSoonContent(
            state = HomeComingSoonState(),
        )
    }
}
