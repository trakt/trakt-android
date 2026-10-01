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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

@Composable
internal fun ActivityEpisodeItemView(
    item: HomeActivityItem.EpisodeItem,
    modifier: Modifier = Modifier,
    itemRating: UserRating? = null,
    removeButton: Boolean = false,
    moreButton: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    onRemoveClick: () -> Unit = {},
    onShowClick: () -> Unit = {},
    onUserClick: (user: User) -> Unit = { _ -> },
    onRateClick: ((Int?) -> Unit)? = null,
) {
    HorizontalMediaCard(
        title = "",
        more = moreButton,
        onClick = onClick,
        onLongClick = onLongClick,
        containerImageUrl =
            item.episode.images?.getScreenshotUrl()
                ?: item.show.images?.getFanartUrl(),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier
                        .weight(1F, fill = false)
                        .onClick(onClick = onShowClick),
                ) {
                    Text(
                        text = item.show.title,
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = item.episode.seasonEpisodeString(),
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (removeButton) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = null,
                        tint = Red500,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(18.dp)
                            .onClickCombined(
                                onClick = onRemoveClick,
                            ),
                    )
                }

                when {
                    onRateClick != null -> ActivityRateButton(
                        rating = itemRating,
                        title = item.show.title,
                        onRateClick = onRateClick,
                        modifier = Modifier.padding(start = 12.dp),
                    )

                    itemRating != null -> ActivityItemRating(
                        rating = itemRating,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
        },
        modifier = modifier,
    )
}

private val PreviewEpisodeItem = HomeActivityItem.EpisodeItem(
    id = 1,
    activity = "watch",
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

@Preview
@Composable
private fun EpisodeItemViewPreview() {
    TraktTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // No rating, rating button disabled.
            ActivityEpisodeItemView(
                item = PreviewEpisodeItem,
            )
            // Rated, rating button disabled.
            ActivityEpisodeItemView(
                item = PreviewEpisodeItem,
                itemRating = PreviewEpisodeRating,
            )
            // Unrated, rating button enabled.
            ActivityEpisodeItemView(
                item = PreviewEpisodeItem,
                onRateClick = {},
            )
            // Rated, rating button enabled.
            ActivityEpisodeItemView(
                item = PreviewEpisodeItem,
                itemRating = PreviewEpisodeRating,
                onRateClick = {},
            )
        }
    }
}
