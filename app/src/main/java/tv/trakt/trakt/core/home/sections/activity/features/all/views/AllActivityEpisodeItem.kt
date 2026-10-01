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
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.views.ActivityItemRating
import tv.trakt.trakt.core.home.sections.activity.views.ActivityRateButton
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant
import java.time.format.DateTimeFormatter

@Composable
internal fun AllActivityEpisodeItem(
    item: HomeActivityItem.EpisodeItem,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemRating: UserRating? = null,
    moreButton: Boolean = false,
    dateFormat: DateTimeFormatter? = null,
    onClick: (() -> Unit)? = null,
    onShowClick: (() -> Unit)? = null,
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
        title = item.show.title,
        titleOriginal = null,
        subtitle = item.episode.seasonEpisodeString(),
        contentImageUrl = item.show.images?.getPosterUrl(),
        containerImageUrl = item.episode.images?.getScreenshotUrl(THUMB)
            ?: item.episode.images?.getFanartUrl(THUMB),
        onClick = onClick,
        onImageClick = onShowClick,
        onLongClick = onLongClick,
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
                            // The local ratings are the source of truth here, so removals show.
                            rating = itemRating,
                            title = item.show.title,
                            starSize = 15.dp,
                            onRateClick = onRateClick,
                            // Offsets the button touch padding to keep the content edges aligned.
                            modifier = Modifier.offset(x = 4.dp, y = 4.dp),
                        )

                        rating != null -> ActivityItemRating(
                            rating = rating,
                            starSize = 15.dp,
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

private val PreviewEpisodeItem = HomeActivityItem.EpisodeItem(
    id = 1L,
    activity = "watched",
    activityAt = Instant.now(),
    user = PreviewData.user1,
    userRating = null,
    show = PreviewData.show1,
    episode = PreviewData.episode1,
)

private val PreviewEpisodeRating = UserRating(
    mediaId = PreviewData.episode1.ids.trakt,
    mediaType = MediaType.Episode,
    rating = 7,
)

@Preview(
    widthDp = 400,
)
@Composable
private fun AllActivityEpisodeItemPreview() {
    TraktTheme {
        Column(verticalArrangement = spacedBy(16.dp)) {
            // Rated, rating button disabled.
            AllActivityEpisodeItem(
                item = PreviewEpisodeItem,
                itemRating = PreviewEpisodeRating,
                modifier = Modifier.fillMaxWidth(),
            )
            // Unrated, rating button enabled.
            AllActivityEpisodeItem(
                item = PreviewEpisodeItem,
                onRateClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
            // Rated, rating button enabled.
            AllActivityEpisodeItem(
                item = PreviewEpisodeItem,
                itemRating = PreviewEpisodeRating,
                onRateClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
