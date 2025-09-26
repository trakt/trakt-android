package tv.trakt.trakt.core.lists.sections.watchlist.features.all.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.ui.theme.colors.White
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllWatchlistShowView(
    item: WatchlistItem.ShowItem,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val genresText = remember(item.show.genres) {
        item.show.genres.take(2).joinToString(", ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    val isReleased = remember(item.show.released) {
        item.show.released?.isBefore(nowUtc()) ?: false
    }

    PanelMediaCard(
        modifier = modifier,
        title = item.show.title,
        titleOriginal = item.show.titleOriginal,
        subtitle = genresText,
        contentImageUrl = item.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(Images.Size.THUMB),
        onLongClick = onLongClick,
        footerContent = {
            if (!isReleased) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Companion.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar_upcoming),
                        contentDescription = null,
                        tint = TraktTheme.colors.textSecondary,
                        modifier = Modifier.Companion.size(14.dp),
                    )
                    Text(
                        text = item.show.released?.relativeDateTimeString() ?: "",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.Absolute.spacedBy(TraktTheme.spacing.chipsSpace),
                    verticalAlignment = Alignment.Companion.CenterVertically,
                ) {
                    val epsString = stringResource(
                        R.string.tag_text_number_of_episodes,
                        item.show.airedEpisodes,
                    )
                    val metaString = remember {
                        val separator = "  •  "
                        buildString {
                            item.released?.let {
                                append(it.year)
                            }
                            if (item.show.airedEpisodes > 0) {
                                if (isNotEmpty()) append(separator)
                                append(epsString)
                            }
                            if (!item.show.certification.isNullOrBlank()) {
                                if (isNotEmpty()) append(separator)
                                append(item.show.certification)
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Companion.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_shows_off),
                            contentDescription = null,
                            tint = TraktTheme.colors.textSecondary,
                            modifier = Modifier.Companion
                                .size(14.dp)
                                .graphicsLayer {
                                    translationY = -1.dp.toPx()
                                },
                        )
                        Text(
                            text = metaString,
                            color = TraktTheme.colors.textSecondary,
                            style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Companion.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.spacedBy(4.dp),
                    ) {
                        val grayFilter = remember {
                            ColorFilter.Companion.colorMatrix(
                                ColorMatrix().apply {
                                    setToSaturation(0F)
                                },
                            )
                        }
                        val whiteFilter = remember {
                            ColorFilter.Companion.tint(White)
                        }

                        Spacer(modifier = Modifier.Companion.weight(1F))

                        Image(
                            painter = painterResource(R.drawable.ic_trakt_icon),
                            contentDescription = null,
                            modifier = Modifier.Companion.size(14.dp),
                            colorFilter = if (item.show.rating.rating > 0) whiteFilter else grayFilter,
                        )
                        Text(
                            text = if (item.show.rating.rating > 0) "${item.show.rating.ratingPercent}%" else "-",
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                        )
                    }
                }
            }
        },
    )
}
