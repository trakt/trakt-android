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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.uppercaseWords
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.core.summary.people.ListEmptyView
import tv.trakt.trakt.core.summary.people.ListLoadingView
import tv.trakt.trakt.core.summary.people.model.PersonCreditItem
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.FilterChip
import tv.trakt.trakt.ui.components.FilterChipGroup
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MoviesCreditsList(
    loading: LoadingState,
    listItems: ImmutableMap<String, ImmutableList<PersonCreditItem.MovieItem>>,
    userCollection: UserCollectionState,
    person: Person,
    modifier: Modifier = Modifier,
    sectionPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onClick: ((Movie) -> Unit)? = null,
    onLongClick: ((Movie) -> Unit)? = null,
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
                title = stringResource(R.string.page_title_movies),
                subtitle = when (listItems.keys.size) {
                    1 -> stringResource(R.string.translated_value_position_acting)
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
                        var selectedFilter by rememberSaveable {
                            mutableStateOf(person.knownForDepartment ?: listItems.keys.first())
                        }

                        Column {
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
                                    key = { it.key },
                                ) { item ->
                                    VerticalMediaCard(
                                        title = item.title,
                                        imageUrl = item.images?.getPosterUrl(),
                                        watched = userCollection.isWatched(item.id, MOVIE),
                                        watchlist = userCollection.isWatchlist(item.id, MOVIE),
                                        onClick = { onClick?.invoke(item.movie) },
                                        onLongClick = { onLongClick?.invoke(item.movie) },
                                        chipSpacing = 10.dp,
                                        chipContent = { modifier ->
                                            val footerText = remember {
                                                val runtime = item.runtime?.inWholeMinutes
                                                val year = item.released?.year
                                                    ?: item.movie.year
                                                    ?: "TBA"

                                                if (runtime != null) {
                                                    "$year • ${runtime.durationFormat()}"
                                                } else {
                                                    "$year"
                                                }
                                            }

                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(0.dp),
                                                modifier = modifier,
                                            ) {
                                                Text(
                                                    text = footerText,
                                                    style = TraktTheme.typography.cardTitle,
                                                    color = TraktTheme.colors.textPrimary,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = modifier,
                                                )

                                                Text(
                                                    text = (item.credit ?: "")
                                                        .ifBlank { "N/A" }
                                                        .uppercaseWords(),
                                                    style = TraktTheme.typography.cardSubtitle,
                                                    color = TraktTheme.colors.textSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
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
