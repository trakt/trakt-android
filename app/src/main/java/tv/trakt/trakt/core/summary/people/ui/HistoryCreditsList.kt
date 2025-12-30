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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.people.ListEmptyView
import tv.trakt.trakt.core.summary.people.ListLoadingView
import tv.trakt.trakt.core.summary.people.model.PersonCreditItem
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HistoryCreditsList(
    loading: LoadingState,
    personShowCredits: ImmutableMap<String, ImmutableList<PersonCreditItem.ShowItem>>?,
    personMovieCredits: ImmutableMap<String, ImmutableList<PersonCreditItem.MovieItem>>?,
    userCollection: UserCollectionState,
    modifier: Modifier = Modifier,
    sectionPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onShowClick: ((Show) -> Unit)? = null,
    onMovieClick: ((Movie) -> Unit)? = null,
    onShowLongClick: ((Show) -> Unit)? = null,
    onMovieLongClick: ((Movie) -> Unit)? = null,
) {
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 1F,
            behindFraction = 1F,
        ),
    )

    val listItems = remember(
        personShowCredits?.size,
        personMovieCredits?.size,
    ) {
        val shows = personShowCredits?.values?.flatten()
            ?.filter { userCollection.isWatched(it.id, it.type) }
            ?: EmptyImmutableList

        val movies = personMovieCredits?.values?.flatten()
            ?.filter { userCollection.isWatched(it.id, it.type) }
            ?: EmptyImmutableList

        (shows + movies)
            .distinctBy { it.key }
            .sortedByDescending { it.released }
            .toImmutableList()
    }

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
                title = stringResource(R.string.list_title_from_my_history),
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
                            LazyRow(
                                state = listState,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainRowSpace),
                                contentPadding = contentPadding,
                            ) {
                                items(
                                    items = listItems,
                                    key = { it.key },
                                ) { item ->
                                    when (item) {
                                        is PersonCreditItem.ShowItem -> {
                                            ShowItemCard(
                                                item = item,
                                                onShowClick = { onShowClick?.invoke(it) },
                                                onShowLongClick = { onShowLongClick?.invoke(it) },
                                                modifier = Modifier,
                                            )
                                        }

                                        is PersonCreditItem.MovieItem -> {
                                            MovieItemCard(
                                                item = item,
                                                onMovieClick = { onMovieClick?.invoke(it) },
                                                onMovieLongClick = { onMovieLongClick?.invoke(it) },
                                                modifier = Modifier,
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
    }
}

@Composable
private fun ShowItemCard(
    item: PersonCreditItem.ShowItem,
    onShowClick: (Show) -> Unit,
    onShowLongClick: (Show) -> Unit,
    modifier: Modifier,
) {
    VerticalMediaCard(
        title = item.show.title,
        imageUrl = item.show.images?.getPosterUrl(),
        chipSpacing = 10.dp,
        chipContent = { modifier ->
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = modifier,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_shows_off),
                    contentDescription = null,
                    tint = TraktTheme.colors.chipContent,
                    modifier = Modifier
                        .size(13.dp),
                )

                val airedEpisodes = stringResource(
                    R.string.tag_text_number_of_episodes,
                    item.show.airedEpisodes,
                )

                val footerText = remember {
                    buildString {
                        item.show.released?.let {
                            append(it.year.toString())
                        } ?: append("TBA")

                        if (item.show.airedEpisodes > 0) {
                            append(" • ")
                            append(airedEpisodes)
                        }
                    }
                }

                Text(
                    text = footerText,
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        onClick = { onShowClick(item.show) },
        onLongClick = { onShowLongClick(item.show) },
        modifier = modifier,
    )
}

@Composable
private fun MovieItemCard(
    item: PersonCreditItem.MovieItem,
    onMovieClick: (Movie) -> Unit,
    onMovieLongClick: (Movie) -> Unit,
    modifier: Modifier,
) {
    VerticalMediaCard(
        title = item.movie.title,
        imageUrl = item.movie.images?.getPosterUrl(),
        chipSpacing = 10.dp,
        chipContent = { modifier ->
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = modifier,
            ) {
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_movies_off),
                        contentDescription = null,
                        tint = TraktTheme.colors.chipContent,
                        modifier = Modifier
                            .size(13.dp)
                            .graphicsLayer {
                                translationY = -(0.25).dp.toPx()
                            },
                    )

                    Text(
                        text = remember {
                            val runtime = item.movie.runtime?.inWholeMinutes
                            if (runtime != null) {
                                "${item.movie.year} • ${runtime.durationFormat()}"
                            } else {
                                item.movie.year.toString()
                            }
                        },
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = (item.credit ?: "").ifBlank { "N/A" },
                    style = TraktTheme.typography.cardSubtitle,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        onClick = { onMovieClick(item.movie) },
        onLongClick = { onMovieLongClick(item.movie) },
        modifier = modifier,
    )
}
