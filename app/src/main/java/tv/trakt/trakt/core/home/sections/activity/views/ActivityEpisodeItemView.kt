package tv.trakt.trakt.core.home.sections.activity.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
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
    onRemoveClick: () -> Unit = {},
    onShowClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current

    HorizontalMediaCard(
        title = "",
        more = moreButton,
        onClick = onClick,
        onLongClick = onLongClick,
        containerImageUrl =
            item.episode.images?.getScreenshotUrl()
                ?: item.show.images?.getFanartUrl(),
        cardContent = {
            InfoChip(
                text = item.activityAt.toLocal().relativePastDateString(),
                iconPainter = painterResource(R.drawable.ic_calendar_check),
                containerColor = TraktTheme.colors.chipContainerOnContent,
            )
        },
        cardTopContent = {
            item.user?.let { user ->
                Box(
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier
                        .sizeIn(maxHeight = 26.dp)
                        .onClick {
                            uriHandler.openUri(
                                Config.webUserUrl(user.username),
                            )
                        },
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

                itemRating?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = spacedBy(2.dp),
                        modifier = Modifier.padding(start = 12.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_star_trakt_on),
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = TraktTheme.colors.textPrimary,
                        )
                        Text(
                            text = it.rating5Scale,
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun EpisodeSocialItemViewPreview() {
    TraktTheme {
        ActivityEpisodeItemView(
            item = HomeActivityItem.EpisodeItem(
                id = 1,
                activity = "watch",
                activityAt = Instant.now(),
                user = PreviewData.user1,
                show = PreviewData.show1,
                episode = PreviewData.episode1,
            ),
        )
    }
}
