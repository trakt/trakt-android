package tv.trakt.trakt.app.core.home.sections.social.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.app.core.home.sections.social.model.SocialActivityItem
import tv.trakt.trakt.app.helpers.extensions.nowUtc
import tv.trakt.trakt.app.helpers.extensions.relativePastDateString
import tv.trakt.trakt.app.helpers.extensions.toLocal
import tv.trakt.trakt.app.helpers.preview.PreviewData
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

@Composable
internal fun EpisodeSocialItemView(
    item: SocialActivityItem.EpisodeItem,
    onClick: (TraktId, Episode) -> Unit,
    onFocused: (SocialActivityItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .sizeIn(maxWidth = TraktTheme.size.horizontalMediaCardSize),
    ) {
        HorizontalMediaCard(
            title = "",
            containerImageUrl =
                item.episode.images?.getScreenshotUrl()
                    ?: item.show.images?.getFanartUrl(),
            onClick = {
                onClick(
                    item.show.ids.trakt,
                    item.episode,
                )
            },
            cardTopContent = {
                Box(
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier.sizeIn(maxHeight = 22.dp),
                ) {
                    InfoChip(
                        text = (item.user.name ?: "").ifBlank { item.user.username },
                        containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F),
                        endPadding = 19.dp,
                    )

                    val borderColor = remember(item.user.isAnyVip) {
                        if (item.user.isAnyVip) Color.Red else Color.Transparent
                    }
                    if (item.user.hasAvatar) {
                        AsyncImage(
                            model = item.user.images?.avatar?.full,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.ic_person_placeholder),
                            modifier = Modifier
                                .size(22.dp)
                                .border(1.5.dp, borderColor, CircleShape)
                                .clip(CircleShape),
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_person_placeholder),
                            contentDescription = null,
                            modifier = Modifier
                                .size(22.dp)
                                .border(1.5.dp, borderColor, CircleShape)
                                .clip(CircleShape),
                        )
                    }
                }
            },
            footerContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy((1).dp),
                ) {
                    Text(
                        text = item.show.title,
                        style = TraktTheme.typography.cardTitle,
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Companion.Ellipsis,
                    )

                    Text(
                        text = item.episode.seasonEpisodeString,
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Companion.Ellipsis,
                    )

                    InfoChip(
                        text = item.activityAt.toLocal().relativePastDateString(),
                        iconPainter = painterResource(R.drawable.ic_calendar_check),
                        modifier = Modifier.padding(top = 7.dp),
                    )
                }
            },
            modifier = Modifier
                .onFocusChanged {
                    if (it.isFocused) {
                        onFocused(item)
                    }
                },
        )
    }
}

@Preview
@Composable
private fun EpisodeSocialItemViewPreview() {
    TraktTheme {
        EpisodeSocialItemView(
            item = SocialActivityItem.EpisodeItem(
                id = 1,
                activity = "watch",
                activityAt = nowUtc(),
                user = PreviewData.user1,
                show = PreviewData.show1,
                episode = PreviewData.episode1,
            ),
            onClick = { _, _ -> },
            onFocused = {},
        )
    }
}
