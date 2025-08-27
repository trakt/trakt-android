package tv.trakt.trakt.app.core.profile.sections.history.viewall

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
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
import tv.trakt.trakt.app.common.model.SyncHistoryItem
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.profile.ProfileConfig.HISTORY_ALL_PAGE_LIMIT
import tv.trakt.trakt.app.core.profile.ProfileConfig.HISTORY_NEXT_PAGE_OFFSET
import tv.trakt.trakt.app.helpers.longDateTimeFormat
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

@Composable
internal fun ProfileHistoryViewAllScreen(
    viewModel: ProfileHistoryViewAllViewModel,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (TraktId, Episode) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(ON_CREATE) {
        viewModel.updateData()
    }

    ProfileHistoryViewAllContent(
        state = state,
        onItemClick = {
            if (state.isLoading || state.isLoadingPage) {
                return@ProfileHistoryViewAllContent
            }
            when (it.type) {
                "movie" -> onNavigateToMovie(it.movie!!.ids.trakt)
                "episode" -> onNavigateToEpisode(it.show?.ids?.trakt!!, it.episode!!)
                else -> throw IllegalArgumentException("Unsupported item type: ${it.type}")
            }
        },
        onLoadNextPage = {
            viewModel.loadNextDataPage()
        },
    )
}

@Composable
private fun ProfileHistoryViewAllContent(
    state: ProfileHistoryViewAllState,
    modifier: Modifier = Modifier,
    onItemClick: (SyncHistoryItem) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    var focusedItem by remember { mutableStateOf<SyncHistoryItem?>(null) }
    var focusedItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    val focusRequesters = remember { mutableMapOf<Long, FocusRequester>() }

    LaunchedEffect(state.isLoading) {
        // Used when list is updated after user comes back and modifies history/watchlist etc.
        if (state.isLoading) {
            focusedItem = null
            focusedItemId = null
            focusRequesters.clear()
        }
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedItemId]?.requestFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedItem?.backdropImageUrl,
            saturation = 0F,
            crossfade = true,
        )

        val gridSpace = TraktTheme.spacing.mainGridSpace
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = spacedBy(gridSpace),
            verticalArrangement = spacedBy(gridSpace * 2),
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = 16.dp,
                top = 30.dp,
                bottom = TraktTheme.spacing.mainContentVerticalSpace,
            ),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.list_title_recently_watched),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .focusProperties {
                            down = focusRequesters.values.firstOrNull() ?: FocusRequester.Default
                        }
                        .focusable(),
                )
            }

            if (state.isLoading && state.items.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            } else if (!state.items.isNullOrEmpty()) {
                items(
                    count = state.items.size,
                    key = { index -> state.items[index].id },
                ) { index ->
                    val item = state.items[index]
                    val focusRequester = focusRequesters.getOrPut(item.id) {
                        FocusRequester()
                    }

                    HorizontalMediaCard(
                        title = "",
                        containerImageUrl = remember(item.type) {
                            item.mediaCardImageUrl
                        },
                        onClick = { onItemClick(item) },
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
                                    text = item.watchedAt.toLocal().format(longDateTimeFormat),
                                    style = TraktTheme.typography.cardSubtitle,
                                    color = TraktTheme.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedItem = item
                                    focusedItemId = item.id

                                    loadNextPageIfNeeded(
                                        size = state.items.size,
                                        index = index,
                                        onLoadNextPage = onLoadNextPage,
                                    )
                                }
                            },
                    )
                }
            }

            if (state.isLoadingPage) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            }
        }
    }

    if (state.error != null) {
        GenericErrorView(
            error = state.error,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = TraktTheme.spacing.mainContentStartSpace,
                    end = TraktTheme.spacing.mainContentEndSpace,
                ),
        )
    }
}

private fun loadNextPageIfNeeded(
    size: Int,
    index: Int,
    onLoadNextPage: () -> Unit,
) {
    if (size >= HISTORY_ALL_PAGE_LIMIT && index >= size - HISTORY_NEXT_PAGE_OFFSET) {
        onLoadNextPage()
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
    heightDp = 1000,
)
@Composable
private fun Preview() {
    TraktTheme {
        ProfileHistoryViewAllContent(
            state = ProfileHistoryViewAllState(),
            onItemClick = {},
            onLoadNextPage = {},
        )
    }
}
