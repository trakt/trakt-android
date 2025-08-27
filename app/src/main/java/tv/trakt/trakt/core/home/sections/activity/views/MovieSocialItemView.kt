package tv.trakt.trakt.core.home.sections.activity.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tv.trakt.trakt.app.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InfoChip
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieSocialItemView(
    item: HomeActivityItem.MovieItem,
    onClick: (TraktId) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalMediaCard(
        title = "",
        containerImageUrl = item.movie.images?.getFanartUrl(),
        onClick = { onClick(item.movie.ids.trakt) },
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
                    modifier = Modifier.sizeIn(maxHeight = 22.dp),
                ) {
                    InfoChip(
                        text = (user.name ?: "").ifBlank { user.username },
                        containerColor = TraktTheme.colors.chipContainerOnContent,
                        endPadding = 19.dp,
                    )

                    val borderColor = remember(user.isAnyVip) {
                        if (user.isAnyVip) Color.Red else Color.Transparent
                    }
                    if (user.hasAvatar) {
                        AsyncImage(
                            model = user.images?.avatar?.full,
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
            }
        },
        footerContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = item.movie.title,
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = stringResource(R.string.translated_value_type_movie),
                    style = TraktTheme.typography.cardSubtitle,
                    color = TraktTheme.colors.textSecondary,
                )
            }
        },
        modifier = modifier,
    )
}
