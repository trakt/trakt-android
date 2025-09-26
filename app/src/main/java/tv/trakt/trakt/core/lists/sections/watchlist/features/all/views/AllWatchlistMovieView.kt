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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.extensions.relativeDateString
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.ui.theme.colors.White
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllWatchlistMovieView(
    item: WatchlistItem.MovieItem,
    onLongClick: () -> Unit,
    onCheckClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val genresText = remember(item.movie.genres) {
        item.movie.genres.take(2).joinToString(", ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    val isReleased = remember(item.movie.released) {
        item.movie.released?.isTodayOrBefore() ?: false
    }

    PanelMediaCard(
        modifier = modifier,
        title = item.movie.title,
        titleOriginal = item.movie.titleOriginal,
        subtitle = genresText,
        contentImageUrl = item.movie.images?.getPosterUrl(),
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
                        text = item.movie.released?.relativeDateString() ?: "",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.Absolute.spacedBy(TraktTheme.spacing.chipsSpace),
                    verticalAlignment = Alignment.Companion.CenterVertically,
                ) {
                    val metaString = remember {
                        val separator = "  •  "
                        buildString {
                            item.released?.let {
                                append(it.year)
                            }
                            item.movie.runtime?.let {
                                if (isNotEmpty()) append(separator)
                                append(it.inWholeMinutes.durationFormat())
                            }
                            if (!item.movie.certification.isNullOrBlank()) {
                                if (isNotEmpty()) append(separator)
                                append(item.movie.certification)
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Companion.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_movies_off),
                            contentDescription = null,
                            tint = TraktTheme.colors.textSecondary,
                            modifier = Modifier.Companion.size(14.dp),
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
                            colorFilter = if (item.movie.rating.rating > 0) whiteFilter else grayFilter,
                        )
                        Text(
                            text = if (item.movie.rating.rating > 0) "${item.movie.rating.ratingPercent}%" else "-",
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                        )
                    }
                }
            }
        },
    )
}
