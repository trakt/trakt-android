package tv.trakt.trakt.core.summary.shows.features.seasons.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.summary.shows.features.seasons.model.EpisodeItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelHorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun EpisodeListItem(
    show: Show,
    episode: EpisodeItem,
    onClick: ((EpisodeItem) -> Unit)?,
    onCheckClick: ((EpisodeItem) -> Unit)?,
    onCheckLongClick: ((EpisodeItem) -> Unit)?,
    onMoreClick: ((EpisodeItem) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val isReleased = episode.episode.rememberReleased()

    PanelHorizontalMediaCard(
        title = episode.episode.title,
        subtitle = stringResource(
            R.string.episode_footer_season_episode,
            episode.episode.season,
            episode.episode.number,
        ),
        contentImageUrl = when {
            isReleased -> episode.episode.images?.getScreenshotUrl(Images.Size.THUMB)
                ?: show.images?.getFanartUrl(Images.Size.THUMB)
            else -> show.images?.getFanartUrl(Images.Size.THUMB)
        },
        containerImageUrl = null,
        more = (episode.isWatched || isReleased) && !episode.isLoading,
        watched = episode.isWatched,
        footerContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (!isReleased) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_calendar_upcoming),
                            contentDescription = null,
                            tint = TraktTheme.colors.textPrimary,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = episode.episode.releasedAt?.toLocal()?.relativeDateTimeString()
                                ?: "TBA",
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.cardSubtitle.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.W500,
                            ),
                        )
                    }
                } else {
                    val runtime = episode.episode.runtime?.inWholeMinutes
                    if (runtime != null) {
                        Text(
                            text = rememberDurationFormat(runtime),
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.cardSubtitle.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.W500,
                            ),
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier.size(21.dp),
                ) {
                    when {
                        episode.isLoading -> {
                            FilmProgressIndicator(size = 17.dp)
                        }
                        isReleased && !episode.isWatched && episode.isCheckable -> {
                            Icon(
                                painter = painterResource(R.drawable.ic_check_2),
                                contentDescription = null,
                                tint = TraktTheme.colors.accent,
                                modifier = Modifier
                                    .size(21.dp)
                                    .onClickCombined(
                                        onClick = { onCheckClick?.invoke(episode) },
                                        onLongClick = { onCheckLongClick?.invoke(episode) },
                                    ),
                            )
                        }
                    }
                }
            }
        },
        onClick = { onClick?.invoke(episode) },
        onLongClick = {
            if (episode.isWatched || isReleased) onMoreClick?.invoke(episode)
        },
        onImageClick = { onClick?.invoke(episode) },
        modifier = modifier,
    )
}
