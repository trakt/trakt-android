package tv.trakt.trakt.core.home.sections.activity.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

@Composable
internal fun ActivityMovieItemView(
    item: HomeActivityItem.MovieItem,
    modifier: Modifier = Modifier,
    itemRating: UserRating? = null,
    moreButton: Boolean = false,
    onClick: (TraktId) -> Unit = { },
    onLongClick: (() -> Unit)? = null,
    onUserClick: (user: User) -> Unit = { _ -> },
    onRateClick: ((Int?) -> Unit)? = null,
) {
    HorizontalMediaCard(
        modifier = modifier,
        title = "",
        more = moreButton,
        onClick = { onClick(item.movie.ids.trakt) },
        onLongClick = onLongClick,
        containerImageUrl = item.movie.images?.getFanartUrl(),
        cardContent = {
            val isPast = remember(item.activityAt) {
                !item.activityAt.isAfter(nowUtcInstant())
            }
            if (isPast) {
                InfoChip(
                    text = item.activityAt.toLocal().relativePastDateString(),
                    iconPainter = painterResource(R.drawable.ic_calendar_check),
                    containerColor = TraktTheme.colors.chipContainerOnContent,
                )
            }
        },
        cardTopContent = {
            item.user?.let { user ->
                Box(
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier
                        .sizeIn(maxHeight = 26.dp)
                        .onClick { onUserClick(user) },
                ) {
                    InfoChip(
                        text = user.displayName,
                        containerColor = TraktTheme.colors.chipContainerOnContent,
                        endPadding = 24.dp,
                    )

                    val vipAccent = TraktTheme.colors.vipAccent
                    val borderColor = remember(user.isAnyVip) {
                        if (user.isAnyVip) vipAccent else Color.Transparent
                    }
                    if (user.hasAvatar) {
                        AsyncImage(
                            model = user.images?.avatar?.full,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.ic_person_placeholder),
                            modifier = Modifier
                                .size(26.dp)
                                .border(1.5.dp, borderColor, CircleShape)
                                .clip(CircleShape),
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_person_placeholder),
                            contentDescription = null,
                            modifier = Modifier
                                .size(26.dp)
                                .border(1.5.dp, borderColor, CircleShape)
                                .clip(CircleShape),
                        )
                    }
                }
            }
        },
        footerContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .onClick {
                        onClick(item.movie.ids.trakt)
                    },
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier
                        .weight(1F, fill = false),
                ) {
                    Text(
                        text = item.movie.title,
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = stringResource(R.string.translated_value_type_movie),
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                    )
                }

                when {
                    onRateClick != null -> ActivityRateButton(
                        rating = itemRating,
                        title = item.movie.title,
                        onRateClick = onRateClick,
                        modifier = Modifier.padding(start = 8.dp),
                    )

                    itemRating != null -> ActivityItemRating(
                        rating = itemRating,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
        },
    )
}

private val PreviewMovieItem = HomeActivityItem.MovieItem(
    id = 1,
    activity = "watch",
    activityAt = Instant.now(),
    user = PreviewData.user1,
    userRating = null,
    movie = PreviewData.movie1,
)

private val PreviewMovieRating = UserRating(
    mediaId = PreviewData.movie1.ids.trakt,
    mediaType = MediaType.Movie,
    rating = 9,
)

@Preview
@Composable
private fun MovieItemViewPreview() {
    TraktTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // No rating, rating button disabled.
            ActivityMovieItemView(
                item = PreviewMovieItem,
            )
            // Rated, rating button disabled.
            ActivityMovieItemView(
                item = PreviewMovieItem,
                itemRating = PreviewMovieRating,
            )
            // Unrated, rating button enabled.
            ActivityMovieItemView(
                item = PreviewMovieItem,
                onRateClick = {},
            )
            // Rated, rating button enabled.
            ActivityMovieItemView(
                item = PreviewMovieItem,
                itemRating = PreviewMovieRating,
                onRateClick = {},
            )
        }
    }
}
