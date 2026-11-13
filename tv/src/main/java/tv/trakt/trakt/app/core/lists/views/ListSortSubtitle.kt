package tv.trakt.trakt.app.core.lists.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import tv.trakt.trakt.app.core.lists.model.ListSortValues
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.Sorting
import java.time.Instant

private val sortIconSize = 12.dp

/**
 * Card subtitle showing the value the list is sorted by. Falls back to [default] when the sort
 * has no value worth showing (rank, title) or the item lacks one.
 */
@Composable
internal fun ListSortSubtitle(
    sorting: Sorting,
    values: ListSortValues,
    modifier: Modifier = Modifier,
    style: TextStyle = TraktTheme.typography.cardTitle,
    color: Color = TraktTheme.colors.textPrimary,
    default: @Composable () -> Unit,
) {
    val dateFormat = mediumDateFormat()
    val text = when (sorting.type) {
        SortType.Rank, SortType.Title -> {
            null
        }

        SortType.Added -> {
            values.listedAt?.toLocalDay()?.format(dateFormat)
        }

        SortType.Released -> {
            values.released?.format(dateFormat)
        }

        SortType.Runtime -> {
            values.runtime
                ?.takeIf { it.isPositive() }
                ?.let { rememberDurationFormat(it.inWholeMinutes) }
        }

        SortType.Rating -> {
            values.rating
                .takeIf { it.rating > 0 }
                ?.let { "${it.ratingPercent}%" }
        }

        SortType.UserRating -> {
            values.userRating
                ?.takeIf { it.rating > 0 }
                ?.rating5Scale
        }
    }

    if (text == null) {
        default()
        return
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = modifier,
    ) {
        sorting.type.displayIconRes?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(sortIconSize),
            )
        }

        Text(
            text = text,
            style = style,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    val movie = PreviewData.movie1
    val values = ListSortValues.of(
        movie = movie,
        listedAt = Instant.parse("2026-03-12T10:00:00Z"),
        userRating = UserRating(
            mediaId = movie.ids.trakt,
            mediaType = MediaType.Movie,
            rating = 9,
        ),
    )

    TraktTheme {
        Column(
            verticalArrangement = spacedBy(8.dp),
        ) {
            SortType.entries.forEach { type ->
                ListSortSubtitle(
                    sorting = Sorting(type = type, order = SortOrder.Desc),
                    values = values,
                ) {
                    Text(
                        text = "Default",
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                    )
                }
            }
        }
    }
}
