package tv.trakt.trakt.core.search.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.search.model.SearchFilter
import tv.trakt.trakt.core.search.model.SearchItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SearchGridItem(
    item: SearchItem,
    filter: SearchFilter,
    modifier: Modifier = Modifier,
    watched: Boolean = false,
    watchlist: Boolean = false,
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onShowLongClick: (Show) -> Unit = {},
    onMovieLongClick: (Movie) -> Unit = {},
    onPersonClick: (Person) -> Unit = {},
) {
    when (item) {
        is SearchItem.Show -> ShowGridItem(
            item = item,
            filter = filter,
            watched = watched,
            watchlist = watchlist,
            onShowClick = onShowClick,
            onShowLongClick = onShowLongClick,
            modifier = modifier,
        )

        is SearchItem.Movie -> MovieGridItem(
            item = item,
            filter = filter,
            watched = watched,
            watchlist = watchlist,
            onMovieClick = onMovieClick,
            onMovieLongClick = onMovieLongClick,
            modifier = modifier,
        )

        is SearchItem.Person -> PersonGridItem(
            item = item,
            onPersonClick = onPersonClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun ShowGridItem(
    item: SearchItem.Show,
    filter: SearchFilter,
    watched: Boolean,
    watchlist: Boolean,
    onShowClick: (Show) -> Unit,
    onShowLongClick: (Show) -> Unit,
    modifier: Modifier,
) {
    var currentFilter by remember { mutableStateOf(filter) }

    LaunchedEffect(filter) {
        if (filter != SearchFilter.PEOPLE) {
            currentFilter = filter
        }
    }

    VerticalMediaCard(
        title = item.show.title,
        imageUrl = item.show.images?.getPosterUrl(),
        watched = watched,
        watchlist = watchlist,
        chipSpacing = 10.dp,
        chipContent = { modifier ->
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(4.dp),
                modifier = modifier,
            ) {
                if (currentFilter == SearchFilter.MEDIA) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shows_off),
                        contentDescription = null,
                        tint = TraktTheme.colors.chipContent,
                        modifier = Modifier
                            .size(13.dp),
                    )
                }

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
private fun MovieGridItem(
    item: SearchItem.Movie,
    filter: SearchFilter,
    watched: Boolean,
    watchlist: Boolean,
    onMovieClick: (Movie) -> Unit,
    onMovieLongClick: (Movie) -> Unit = {},
    modifier: Modifier,
) {
    var currentFilter by remember { mutableStateOf(filter) }

    LaunchedEffect(filter) {
        if (filter != SearchFilter.PEOPLE) {
            currentFilter = filter
        }
    }

    VerticalMediaCard(
        title = item.movie.title,
        imageUrl = item.movie.images?.getPosterUrl(),
        watched = watched,
        watchlist = watchlist,
        chipSpacing = 10.dp,
        chipContent = { modifier ->
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(4.dp),
                modifier = modifier,
            ) {
                if (currentFilter == SearchFilter.MEDIA) {
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
                }

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
        },
        onClick = { onMovieClick(item.movie) },
        onLongClick = { onMovieLongClick(item.movie) },
        modifier = modifier,
    )
}

@Composable
private fun PersonGridItem(
    item: SearchItem.Person,
    onPersonClick: (Person) -> Unit,
    modifier: Modifier,
) {
    VerticalMediaCard(
        title = item.person.name,
        more = false,
        imageUrl = item.person.images?.getHeadshotUrl(),
        chipContent = {
            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Text(
                    text = item.person.name,
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (item.showBirthday) {
                    item.person.birthday?.let { date ->
                        val (isToday, age) = remember(date) {
                            val today = nowLocalDay()
                            Pair(
                                date.monthValue == today.monthValue &&
                                    date.dayOfMonth == today.dayOfMonth,
                                (today.year - date.year) - when {
                                    date.dayOfYear <= today.dayOfYear -> 0
                                    else -> 1
                                },
                            )
                        }

                        Row(
                            horizontalArrangement = spacedBy(4.dp),
                            verticalAlignment = CenterVertically,
                        ) {
                            if (isToday) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_celebration),
                                    contentDescription = null,
                                    tint = TraktTheme.colors.textSecondary,
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                            Text(
                                text = "${date.format(mediumDateFormat)} ($age)",
                                style = TraktTheme.typography.cardSubtitle,
                                color = TraktTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                } else {
                    item.person.knownForDepartment?.let { string ->
                        Text(
                            text = string.replaceFirstChar { it.uppercaseChar() },
                            style = TraktTheme.typography.cardSubtitle,
                            color = TraktTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
        onClick = { onPersonClick(item.person) },
        modifier = modifier,
    )
}
