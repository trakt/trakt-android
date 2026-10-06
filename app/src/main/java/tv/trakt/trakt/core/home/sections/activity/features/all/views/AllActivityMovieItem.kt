package tv.trakt.trakt.core.home.sections.activity.features.all.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.isTraktUnknown
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.views.ActivityItemRating
import tv.trakt.trakt.core.home.sections.activity.views.ActivityRateButton
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant
import java.time.format.DateTimeFormatter

@Composable
internal fun AllActivityMovieItem(
    item: HomeActivityItem.MovieItem,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemRating: UserRating? = null,
    moreButton: Boolean = false,
    dateFormat: DateTimeFormatter? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onUserClick: ((User) -> Unit)? = null,
    onRateClick: ((Int?) -> Unit)? = null,
) {
    val isPast = remember(item.activityAt) {
        !item.activityAt.isAfter(nowUtcInstant())
    }

    val rating = remember(itemRating, item.userRating) {
        itemRating ?: item.userRating
    }

    PanelMediaCard(
        enabled = enabled,
        title = item.title,
        titleOriginal = null,
        subtitle = stringResource(R.string.translated_value_type_movie),
        contentImageUrl = item.movie.images?.getPosterUrl(),
        containerImageUrl = item.movie.images?.getFanartUrl(Images.Size.THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
        onImageClick = onClick,
        more = moreButton,
        footerContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Bottom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = CenterVertically,
                    modifier = Modifier
                        .weight(1F)
                        .alpha(if (isPast) 1.0f else 0f),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar_check),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = when (dateFormat) {
                            null -> item.activityAt.toLocal().relativePastDateString()
                            else -> if (item.activityAt.isTraktUnknown()) {
                                stringResource(R.string.button_text_mark_as_watched_unknown_date)
                            } else {
                                item.activityAt.toLocal().format(dateFormat)
                            }
                        },
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.cardSubtitle.copy(
                            fontWeight = W500,
                        ),
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = spacedBy(8.dp),
                    modifier = Modifier.padding(start = 12.dp),
                ) {
                    when {
                        onRateClick != null -> ActivityRateButton(
                            rating = itemRating,
                            title = item.movie.title,
                            onRateClick = onRateClick,
                            modifier = Modifier.offset(x = 4.dp, y = 4.dp),
                        )

                        rating != null -> ActivityItemRating(
                            rating = rating,
                        )
                    }

                    item.user?.let { user ->
                        AllActivityUserChip(
                            user = user,
                            onUserClick = {
                                onUserClick?.let { it(user) }
                            },
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )
}

private val PreviewMovieItem = HomeActivityItem.MovieItem(
    id = 1L,
    activity = "watched",
    activityAt = Instant.now(),
    user = PreviewData.user1,
    userRating = null,
    movie = PreviewData.movie1,
)

private val PreviewMovieRating = UserRating(
    mediaId = 1.toTraktId(),
    mediaType = MediaType.Movie,
    rating = 8,
)

@Preview(
    widthDp = 400,
)
@Composable
private fun AllActivityMovieItemPreview() {
    TraktTheme {
        Column(verticalArrangement = spacedBy(16.dp)) {
            AllActivityMovieItem(
                item = PreviewMovieItem,
                itemRating = PreviewMovieRating,
                modifier = Modifier.fillMaxWidth(),
            )
            AllActivityMovieItem(
                item = PreviewMovieItem,
                onRateClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
            AllActivityMovieItem(
                item = PreviewMovieItem,
                itemRating = PreviewMovieRating,
                onRateClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
