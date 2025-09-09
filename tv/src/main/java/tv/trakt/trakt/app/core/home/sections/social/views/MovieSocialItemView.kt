package tv.trakt.trakt.app.core.home.sections.social.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.app.core.home.sections.social.model.SocialActivityItem
import tv.trakt.trakt.app.helpers.preview.PreviewData
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

@Composable
internal fun MovieSocialItemView(
    item: SocialActivityItem.MovieItem,
    onClick: (TraktId) -> Unit,
    onFocused: (SocialActivityItem?) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalMediaCard(
        title = "",
        containerImageUrl = item.movie.images?.getFanartUrl(),
        onClick = { onClick(item.movie.ids.trakt) },
        cardTopContent = {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier.sizeIn(maxHeight = 22.dp),
            ) {
                InfoChip(
                    text = item.user.displayName,
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
        cardContent = {
            InfoChip(
                text = item.activityAt.toLocal().relativePastDateString(),
                iconPainter = painterResource(R.drawable.ic_calendar_check),
            )
        },
        footerContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = item.movie.title,
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Companion.Ellipsis,
                )

                Text(
                    text = stringResource(R.string.translated_value_type_movie),
                    style = TraktTheme.typography.cardSubtitle,
                    color = TraktTheme.colors.textSecondary,
                )
            }
        },
        modifier = modifier
            .onFocusChanged {
                if (it.isFocused) {
                    onFocused(item)
                }
            },
    )
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        MovieSocialItemView(
            item = SocialActivityItem.MovieItem(
                id = 1,
                activity = "watch",
                activityAt = nowUtc(),
                user = PreviewData.user1,
                movie = PreviewData.movie1,
            ),
            onClick = {},
            onFocused = {},
        )
    }
}
