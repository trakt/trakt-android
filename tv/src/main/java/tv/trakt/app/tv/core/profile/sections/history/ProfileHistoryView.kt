package tv.trakt.app.tv.core.profile.sections.history

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import tv.trakt.app.tv.R
import tv.trakt.app.tv.common.model.SyncHistoryItem
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.common.ui.PositionFocusLazyRow
import tv.trakt.app.tv.common.ui.mediacards.EpisodeSkeletonCard
import tv.trakt.app.tv.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.app.tv.common.ui.mediacards.HorizontalViewAllCard
import tv.trakt.app.tv.core.episodes.model.Episode
import tv.trakt.app.tv.core.profile.ProfileConfig.PROFILE_SECTION_LIMIT
import tv.trakt.app.tv.helpers.extensions.emptyFocusListItems
import tv.trakt.app.tv.helpers.extensions.relativePastDateTimeString
import tv.trakt.app.tv.helpers.extensions.toLocal
import tv.trakt.app.tv.ui.theme.TraktTheme

private val sections = listOf(
    "content",
)

@Composable
internal fun ProfileHistoryView(
    modifier: Modifier = Modifier,
    viewModel: ProfileHistoryViewModel = koinViewModel(),
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onLoaded: () -> Unit = {},
    onFocused: (SyncHistoryItem?) -> Unit = {},
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

    LifecycleEventEffect(ON_CREATE) {
        viewModel.updateData()
    }

    ProfileHistoryContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        focusRequesters = focusRequesters,
        onLoaded = onLoaded,
        onFocused = onFocused,
        onViewAllClick = onViewAllClick,
        onClick = {
            when (it.type) {
                "movie" -> onMovieClick(it.movie!!.ids.trakt)
                "episode" -> onEpisodeClick(it.show?.ids?.trakt!!, it.episode!!)
                else -> throw IllegalArgumentException("Unsupported item type: ${it.type}")
            }
        },
    )
}

@Composable
internal fun ProfileHistoryContent(
    state: ProfileHistoryState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    focusRequesters: Map<String, FocusRequester> = emptyMap(),
    onLoaded: () -> Unit = {},
    onFocused: (SyncHistoryItem?) -> Unit = {},
    onClick: (SyncHistoryItem) -> Unit = {},
    onViewAllClick: () -> Unit = {},
) {
    var hasLoaded by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.header_recently_watched),
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

            state.items?.isEmpty() == true -> {
                Text(
                    text = stringResource(R.string.info_generic_empty_list),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier.padding(headerPadding),
                )
            }

            else -> {
                LaunchedEffect(Unit) {
                    if (state.items != null && !hasLoaded) {
                        onLoaded()
                        hasLoaded = true
                    }
                }
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
    PositionFocusLazyRow(
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

        if (items().size >= PROFILE_SECTION_LIMIT) {
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
        containerImageUrl = remember(item.type) {
            item.mediaCardImageUrl
        },
        onClick = onClick,
        footerContent = {
            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Text(
                    text = remember(item.type) {
                        when (item.type) {
                            "show" -> item.show!!.title
                            "movie" -> item.movie!!.title
                            "episode" -> item.episode!!.seasonEpisodeString
                            else -> "TBA"
                        }
                    },
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = item.watchedAt.toLocal().relativePastDateTimeString(),
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
        ProfileHistoryContent(
            state = ProfileHistoryState(
                isLoading = false,
                items = emptyList<SyncHistoryItem>().toImmutableList(),
            ),
        )
    }
}
