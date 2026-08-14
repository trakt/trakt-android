package tv.trakt.trakt.core.summary.social.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.MediaType.Show
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.profile.sections.social.ui.SocialUserView
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity.Watched.Rated
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.theme.TraktTheme
import java.util.Locale
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Composable
internal fun MediaSocialItemCard(
    item: MediaSocialActivity,
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    containerColor: Color = TraktTheme.colors.panelCardContainer,
    onClick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .shadow(
                elevation = TraktTheme.colors.shadowDefault,
                shape = RoundedCornerShape(corner),
            )
            .graphicsLayer {
                clip = false
            }
            .background(containerColor, RoundedCornerShape(corner))
            .onClickCombined(onClick = onClick)
            .padding(12.dp),
    ) {
        SocialUserView(
            user = item.user,
            size = 52.dp,
            showName = false,
            onUserClick = onClick,
        )

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(start = 12.dp)
                .padding(end = 4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = item.user.displayName,
                        style = TraktTheme.typography.cardTitle.copy(fontSize = 16.sp),
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp),
                    )

                    Text(
                        text = item.lastActivityAt.toLocal().relativePastDateString(),
                        style = TraktTheme.typography.cardSubtitle.copy(fontSize = 11.sp),
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                val chipContainer = when {
                    TraktTheme.colors.isLight -> TraktTheme.colors.chipContainerOnContent
                    else -> TraktTheme.colors.chipContainer
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1F)
                        .padding(end = 4.dp),
                ) {
                    item.watched?.let {
                        val showText = buildString {
                            append(stringResource(R.string.text_play_count, it.plays))
                            it.duration?.let { duration ->
                                append("  •  ")
                                append(rememberDurationFormat(duration.inWholeMinutes))
                            }
                        }

                        val movieText = buildString {
                            if (it.plays > 1) {
                                append(stringResource(R.string.text_play_count, it.plays))
                            } else {
                                append(stringResource(R.string.tag_text_watched))
                            }
                        }

                        InfoChip(
                            iconPainter = painterResource(R.drawable.ic_check_double),
                            iconPadding = 1.dp,
                            containerColor = chipContainer,
                            text = when {
                                item.type == Show -> showText
                                else -> movieText
                            },
                        )
                    }

                    item.watchlist?.let {
                        InfoChip(
                            iconPainter = painterResource(R.drawable.ic_bookmark_on),
                            containerColor = chipContainer,
                            text = stringResource(R.string.list_title_watchlist),
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    item.watched?.commented?.let {
                        InfoChip(
                            iconPainter = painterResource(R.drawable.ic_comment),
                            containerColor = chipContainer,
                            text = stringResource(R.string.dialog_title_comment),
                        )
                    }

                    item.watched?.rated?.let {
                        val ratingText = remember(it.rating) {
                            "%.1f"
                                .format(Locale.US, it.rating / 2f)
                                .removeSuffix(".0")
                                .removeSuffix(",0")
                        }
                        InfoChip(
                            iconPainter = painterResource(R.drawable.ic_star),
                            containerColor = chipContainer,
                            text = ratingText,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@DevicePreview
@Composable
private fun PosterPreview() {
    TraktThemeLightDark {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            MediaSocialItemCard(
                item = MediaSocialActivity(
                    type = Show,
                    user = PreviewData.user1,
                    watched = MediaSocialActivity.Watched(
                        lastWatchedAt = nowUtcInstant().minusSeconds(3.days.inWholeSeconds),
                        lastUpdatedAt = nowUtcInstant().minusSeconds(3.days.inWholeSeconds),
                        plays = 3,
                        duration = 5.hours,
                        rated = Rated(
                            rating = 8,
                            ratedAt = nowUtcInstant().minusSeconds(3.days.inWholeSeconds),
                        ),
                        commented = MediaSocialActivity.Watched.Commented(
                            id = 1.toTraktId(),
                            comment = "Sample comment by someone",
                            spoiler = false,
                            review = false,
                            createdAt = nowUtcInstant(),
                            updatedAt = nowUtcInstant(),
                        ),
                    ),
                    watchlist = MediaSocialActivity.Watchlist(
                        listedAt = nowUtcInstant().minusSeconds(3.days.inWholeSeconds),
                    ),
                ),
            )
        }
    }
}
