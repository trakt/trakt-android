package tv.trakt.trakt.app.core.search.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.lists.CustomListCard
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.core.search.model.SearchItem
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.resources.R
import java.time.LocalDate

@Composable
internal fun SearchGridItem(
    item: SearchItem,
    collection: UserCollectionState,
    modifier: Modifier = Modifier,
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onPersonClick: (Person) -> Unit = {},
    onListClick: (CustomList) -> Unit = {},
) {
    when (item) {
        is SearchItem.Show -> ShowItem(
            item = item.show,
            collection = collection,
            onClick = { onShowClick(item.show) },
            modifier = modifier,
        )

        is SearchItem.Movie -> MovieItem(
            item = item.movie,
            collection = collection,
            onClick = { onMovieClick(item.movie) },
            modifier = modifier,
        )

        is SearchItem.Person -> PersonItem(
            item = item,
            onClick = { onPersonClick(item.person) },
            modifier = modifier,
        )

        is SearchItem.List -> CustomListCard(
            list = item.list,
            likesVisible = true,
            liked = item.liked,
            onClick = { onListClick(item.list) },
            modifier = modifier
                .fillMaxWidth()
                .height(TraktTheme.size.detailsCustomListSize),
        )
    }
}

@Composable
private fun ShowItem(
    item: Show,
    collection: UserCollectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VerticalMediaCard(
        title = item.title,
        watched = collection.isWatched(item.ids.trakt, MediaType.Show, item.airedEpisodes),
        watching = collection.isWatching(item.ids.trakt, MediaType.Show, item.airedEpisodes),
        watchlist = collection.isWatchlist(item.ids.trakt, MediaType.Show),
        imageUrl = item.images?.getPosterUrl(),
        onClick = onClick,
        chipContent = {
            val episodes = item.airedEpisodes.takeIf { it > 0 }
                ?.let { stringResource(R.string.tag_text_number_of_episodes, it) }
            val text = listOfNotNull(item.year?.toString(), episodes)
                .joinToString("  •  ")

            CardTitle(
                text = text.ifBlank { stringResource(R.string.translated_value_type_show) },
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun MovieItem(
    item: Movie,
    collection: UserCollectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VerticalMediaCard(
        title = item.title,
        watched = collection.isWatched(item.ids.trakt, MediaType.Movie, null),
        watchlist = collection.isWatchlist(item.ids.trakt, MediaType.Movie),
        imageUrl = item.images?.getPosterUrl(),
        onClick = onClick,
        chipContent = {
            CardTitle(
                text = item.yearString +
                    "  •  ${rememberDurationFormat(item.runtime?.inWholeMinutes)}",
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun PersonItem(
    item: SearchItem.Person,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VerticalMediaCard(
        title = item.person.name,
        imageUrl = item.person.images?.getHeadshotUrl(),
        onClick = onClick,
        chipContent = {
            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                CardTitle(text = item.person.name)

                val birthday = item.person.birthday
                if (item.showBirthday && birthday != null) {
                    PersonBirthday(date = birthday)
                } else {
                    item.person.knownForDepartment?.let {
                        CardSubtitle(text = it.replaceFirstChar(Char::uppercaseChar))
                    }
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun PersonBirthday(date: LocalDate) {
    val (isToday, age) = remember(date) {
        val today = nowLocalDay()
        Pair(
            date.monthValue == today.monthValue && date.dayOfMonth == today.dayOfMonth,
            (today.year - date.year) - if (date.dayOfYear <= today.dayOfYear) 0 else 1,
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
                modifier = Modifier.size(12.dp),
            )
        }
        CardSubtitle(
            text = "${date.format(mediumDateFormat()).capitalize()} ($age)",
        )
    }
}

@Composable
private fun CardTitle(text: String) {
    Text(
        text = text,
        style = TraktTheme.typography.cardTitle,
        color = TraktTheme.colors.textPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun CardSubtitle(text: String) {
    Text(
        text = text,
        style = TraktTheme.typography.cardSubtitle,
        color = TraktTheme.colors.textSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
