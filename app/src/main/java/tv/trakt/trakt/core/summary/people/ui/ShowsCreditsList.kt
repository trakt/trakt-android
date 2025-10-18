@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.people.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.people.ListEmptyView
import tv.trakt.trakt.core.summary.people.ListLoadingView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.FilterChip
import tv.trakt.trakt.ui.components.FilterChipGroup
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowsCreditsList(
    loading: LoadingState,
    listItems: ImmutableMap<String, ImmutableList<Show>>,
    modifier: Modifier = Modifier,
    sectionPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onClick: ((Show) -> Unit)? = null,
    onLongClick: ((Show) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 1F,
            behindFraction = 1F,
        ),
    )

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sectionPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
        ) {
            TraktHeader(
                title = stringResource(R.string.page_title_shows),
                subtitle = when (listItems.keys.size) {
                    1 -> stringResource(R.string.text_job_acting)
                    else -> null
                },
            )
        }

        Crossfade(
            targetState = loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                IDLE, LOADING -> {
                    ListLoadingView(
                        visible = loading.isLoading,
                        contentPadding = contentPadding,
                    )
                }
                DONE -> {
                    if (listItems.isEmpty()) {
                        ListEmptyView(
                            contentPadding = sectionPadding,
                        )
                    } else {
                        Column {
                            var selectedFilter by remember {
                                mutableStateOf(listItems.keys.first())
                            }

                            if (listItems.keys.size > 1) {
                                ListFilters(
                                    filters = listItems.keys,
                                    selected = selectedFilter,
                                    onSelected = {
                                        selectedFilter = it
                                        scope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                    },
                                )
                            }

                            LazyRow(
                                state = listState,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainRowSpace),
                                contentPadding = contentPadding,
                            ) {
                                items(
                                    items = listItems[selectedFilter] ?: EmptyImmutableList,
                                    key = { it.ids.trakt.value },
                                ) { item ->
                                    VerticalMediaCard(
                                        title = item.title,
                                        imageUrl = item.images?.getPosterUrl(),
                                        onClick = { onClick?.invoke(item) },
                                        onLongClick = { onLongClick?.invoke(item) },
                                        chipContent = {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(
                                                    TraktTheme.spacing.chipsSpace,
                                                ),
                                            ) {
                                                item.year?.let {
                                                    InfoChip(text = it.toString())
                                                }
                                                if (item.airedEpisodes > 0) {
                                                    InfoChip(
                                                        text = stringResource(
                                                            R.string.tag_text_number_of_episodes,
                                                            item.airedEpisodes,
                                                        ),
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier.animateItem(
                                            fadeInSpec = null,
                                            fadeOutSpec = null,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListFilters(
    filters: ImmutableSet<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    FilterChipGroup(
        paddingHorizontal = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
        ),
        paddingVertical = PaddingValues(bottom = 18.dp),
    ) {
        for (filter in filters) {
            FilterChip(
                text = filter.replaceFirstChar { it.uppercase() },
                selected = selected == filter,
                onClick = {
                    onSelected(filter)
                },
            )
        }
    }
}
