package tv.trakt.trakt.app.core.profile.sections.library

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.mediacards.EpisodeSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalViewAllCard
import tv.trakt.trakt.app.core.profile.ProfileConfig.PROFILE_LIBRARY_SECTION_LIMIT
import tv.trakt.trakt.app.core.profile.sections.library.model.LibraryItem
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

private val sections = listOf(
    "content",
)

@Composable
internal fun ProfileLibraryView(
    modifier: Modifier = Modifier,
    viewModel: ProfileLibraryViewModel = koinViewModel(),
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (LibraryItem?) -> Unit = {},
    onMovieClick: (TraktId) -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onViewAllClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    ProfileLibraryContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        focusRequesters = focusRequesters,
        onFocused = onFocused,
        onViewAllClick = onViewAllClick,
        onClick = {
            when (it) {
                is LibraryItem.MovieItem -> onMovieClick(it.movie.ids.trakt)
                is LibraryItem.EpisodeItem -> onEpisodeClick(it.show.ids.trakt, it.episode)
            }
        },
    )
}

@Composable
internal fun ProfileLibraryContent(
    state: ProfileLibraryState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    focusRequesters: Map<String, FocusRequester> = emptyMap(),
    onFocused: (LibraryItem?) -> Unit = {},
    onClick: (LibraryItem) -> Unit = {},
    onViewAllClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = spacedBy(2.dp),
            modifier = Modifier.padding(headerPadding),
        ) {
            Text(
                text = stringResource(R.string.translated_value_library_plex),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )
            if (!state.isLoading && state.items?.isEmpty() == true) {
                Text(
                    text = stringResource(R.string.text_plex_tv_library_description),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta.copy(fontWeight = W400),
                )
            }
        }

        when {
            state.isLoading -> {
                ContentLoadingList(
                    contentPadding = contentPadding,
                )
            }

            state.items?.isEmpty() == true -> {
                Text(
                    text = stringResource(R.string.list_placeholder_empty),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier.padding(headerPadding),
                )
            }

            else -> {
                ContentList(
                    items = { state.items ?: emptyList<LibraryItem>().toImmutableList() },
                    onFocused = onFocused,
                    onClick = onClick,
                    onViewAllClick = onViewAllClick,
                    contentPadding = contentPadding,
                    focusRequesters = focusRequesters,
                )
            }
        }
    }
}

@Composable
private fun ContentList(
    items: () -> ImmutableList<LibraryItem>,
    onFocused: (LibraryItem?) -> Unit,
    onClick: (LibraryItem) -> Unit,
    onViewAllClick: () -> Unit,
    contentPadding: PaddingValues,
    focusRequesters: Map<String, FocusRequester> = emptyMap(),
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
        modifier = Modifier.focusRequester(
            focusRequesters["content"] ?: FocusRequester.Default,
        ),
    ) {
        items(
            items = items(),
            key = { it.key },
        ) { item ->
            ContentListItem(
                item = item,
                onClick = { onClick(item) },
                onFocused = onFocused,
            )
        }

        if (items().size >= PROFILE_LIBRARY_SECTION_LIMIT) {
            item {
                HorizontalViewAllCard(
                    onClick = onViewAllClick,
                    modifier = Modifier
                        .onFocusChanged {
                            if (it.isFocused) {
                                onFocused(null)
                            }
                        },
                )
            }
        }

        emptyFocusListItems()
    }
}

@Composable
private fun ContentListItem(
    item: LibraryItem,
    onClick: () -> Unit,
    onFocused: (LibraryItem) -> Unit,
) {
    HorizontalMediaCard(
        title = "",
        containerImageUrl = remember(item.key) {
            when (item) {
                is LibraryItem.MovieItem -> item.movie.images?.getFanartUrl()
                is LibraryItem.EpisodeItem -> item.show.images?.getFanartUrl()
            }
        },
        onClick = onClick,
        footerContent = {
            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Text(
                    text = remember(item.key) {
                        when (item) {
                            is LibraryItem.MovieItem -> item.movie.title
                            is LibraryItem.EpisodeItem -> item.show.title
                        }
                    },
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (item is LibraryItem.EpisodeItem) {
                    Text(
                        text = item.episode.seasonEpisodeString(),
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (item is LibraryItem.MovieItem) {
                    val durationText = remember(item.movie.runtime) {
                        item.movie.runtime?.inWholeMinutes?.durationFormat() ?: ""
                    }
                    Text(
                        text = durationText,
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        modifier = Modifier
            .onFocusChanged {
                if (it.isFocused) onFocused(item)
            },
    )
}

@Composable
private fun ContentLoadingList(contentPadding: PaddingValues) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(count = 10) {
            EpisodeSkeletonCard()
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
        ProfileLibraryContent(
            state = ProfileLibraryState(
                isLoading = false,
                items = emptyList<LibraryItem>().toImmutableList(),
            ),
        )
    }
}
