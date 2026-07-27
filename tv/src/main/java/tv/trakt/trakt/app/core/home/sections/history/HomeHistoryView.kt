package tv.trakt.trakt.app.core.home.sections.history

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.app.common.model.SyncHistoryItem
import tv.trakt.trakt.app.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.app.common.ui.chips.InfoChip
import tv.trakt.trakt.app.common.ui.mediacards.EpisodeSkeletonCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalViewAllCard
import tv.trakt.trakt.app.core.home.HomeConfig.HOME_SECTION_LIMIT
import tv.trakt.trakt.app.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

private val sections = listOf(
    "content",
)

@Composable
internal fun HomeHistoryView(
    modifier: Modifier = Modifier,
    viewModel: HomeHistoryViewModel = koinViewModel(),
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onFocused: (SyncHistoryItem?) -> Unit = {},
    onLoaded: () -> Unit = {},
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToViewAll: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && state.items != null) {
            onLoaded()
        }
    }

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    LifecycleEventEffect(ON_CREATE) {
        viewModel.updateData()
    }

    HomeHistoryContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        focusRequesters = focusRequesters,
        onFocused = onFocused,
        onViewAllClick = onNavigateToViewAll,
        onClick = {
            when (it.type) {
                "movie" -> onNavigateToMovie(it.movie!!.ids.trakt)
                "episode" -> onNavigateToEpisode(it.show?.ids?.trakt!!, it.episode!!)
                else -> throw IllegalArgumentException("Unsupported item type: ${it.type}")
            }
        },
    )
}

@Composable
internal fun HomeHistoryContent(
    state: HomeHistoryState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    focusRequesters: Map<String, FocusRequester> = emptyMap(),
    onFocused: (SyncHistoryItem?) -> Unit = {},
    onClick: (SyncHistoryItem) -> Unit = {},
    onViewAllClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.list_title_history),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(headerPadding),
        )

        when {
            state.isLoading -> {
                ContentLoadingList(
                    contentPadding = contentPadding,
                    onFocused = { onFocused(null) },
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
                    items = { state.items ?: emptyList<SyncHistoryItem>().toImmutableList() },
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
    items: () -> ImmutableList<SyncHistoryItem>,
    onFocused: (SyncHistoryItem?) -> Unit,
    onClick: (SyncHistoryItem) -> Unit,
    onViewAllClick: () -> Unit,
    contentPadding: PaddingValues,
    focusRequesters: Map<String, FocusRequester> = emptyMap(),
) {
    val state = rememberLazyListState()

    var itemsHash by rememberSaveable { mutableIntStateOf(items().hashCode()) }
    LaunchedEffect(items().hashCode()) {
        if (itemsHash != items().hashCode()) {
            itemsHash = items().hashCode()
            state.animateScrollToItem(0)
        }
    }

    PositionFocusLazyRow(
        state = state,
        contentPadding = contentPadding,
        modifier = Modifier.focusRequester(
            focusRequesters["content"] ?: FocusRequester.Default,
        ),
    ) {
        items(
            items = items(),
            key = { it.id },
        ) { item ->
            ContentListItem(
                item = item,
                onClick = { onClick(item) },
                onFocused = onFocused,
            )
        }

        if (items().size >= HOME_SECTION_LIMIT) {
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
    item: SyncHistoryItem,
    onClick: () -> Unit,
    onFocused: (SyncHistoryItem) -> Unit,
) {
    HorizontalMediaCard(
        title = "",
        containerImageUrl = item.mediaCardImageUrl,
        onClick = onClick,
        cardContent = {
            InfoChip(
                text = item.watchedAt.toLocal().relativePastDateString(),
                iconPainter = painterResource(R.drawable.ic_calendar_check),
                containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F),
            )
        },
        footerContent = {
            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Text(
                    text = remember(item.type) {
                        when (item.type) {
                            "show", "episode" -> item.show!!.title
                            "movie" -> item.movie!!.title
                            else -> "TBA"
                        }
                    },
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val seString = item.episode?.seasonEpisodeString() ?: ""
                val durationString = rememberDurationFormat(item.movie?.runtime?.inWholeMinutes)

                val subtext = remember(item.type) {
                    when (item.type) {
                        MediaType.Episode.value -> seString
                        MediaType.Movie.value -> durationString
                        else -> ""
                    }
                }

                Text(
                    text = subtext,
                    style = TraktTheme.typography.cardSubtitle,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        modifier = Modifier
            .onFocusChanged {
                if (it.isFocused) onFocused(item)
            },
    )
}

@Composable
private fun ContentLoadingList(
    contentPadding: PaddingValues,
    onFocused: () -> Unit,
) {
    PositionFocusLazyRow(
        contentPadding = contentPadding,
    ) {
        items(count = 10) {
            EpisodeSkeletonCard(
                modifier = Modifier.onFocusChanged {
                    if (it.isFocused) {
                        onFocused()
                    }
                },
            )
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
        HomeHistoryContent(
            state = HomeHistoryState(
                isLoading = false,
                items = emptyList<SyncHistoryItem>().toImmutableList(),
            ),
        )
    }
}
