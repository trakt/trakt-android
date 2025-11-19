package tv.trakt.trakt.core.movies.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.helpers.extensions.relativeDateString
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SEPARATOR = "  •  "

@Composable
fun MovieMetaFooter(
    movie: Movie,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    rating: Boolean = true,
    check: Boolean = false,
    mediaIcon: Boolean = false,
    onCheckClick: (() -> Unit)? = null,
    onCheckLongClick: (() -> Unit)? = null,
) {
    val metaString = remember {
        buildString {
            movie.released?.let {
                append(it.year)
            }
            movie.runtime?.let {
                if (isNotEmpty()) append(SEPARATOR)
                append(it.inWholeMinutes.durationFormat())
            }
            if (!movie.certification.isNullOrBlank()) {
                if (isNotEmpty()) append(SEPARATOR)
                append(movie.certification)
            }
        }
    }

    val isReleased = remember {
        movie.released?.isTodayOrBefore() ?: false
    }

    Row(
        horizontalArrangement = SpaceBetween,
        verticalAlignment = when {
            check -> Alignment.Bottom
            else -> Alignment.CenterVertically
        },
        modifier = modifier
            .fillMaxWidth(),
    ) {
        if (isReleased) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (mediaIcon) {
                    Icon(
                        painter = painterResource(R.drawable.ic_movies_off),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .size(13.dp)
                            .graphicsLayer {
                                translationY = -0.5.dp.toPx()
                            },
                    )
                }
                Text(
                    text = metaString,
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.cardSubtitle.copy(
                        fontWeight = W500,
                    ),
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Companion.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar_upcoming),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = movie.released?.relativeDateString() ?: "TBA",
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.cardSubtitle.copy(
                        fontWeight = W500,
                    ),
                )
            }
        }

        if (rating && isReleased && !check) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(4.dp),
            ) {
                val grayFilter = remember {
                    ColorFilter.colorMatrix(
                        ColorMatrix().apply {
                            setToSaturation(0F)
                        },
                    )
                }

                Image(
                    painter = painterResource(R.drawable.ic_trakt_icon_color),
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    colorFilter = if (movie.rating.rating > 0) null else grayFilter,
                )
                Text(
                    text = if (movie.rating.rating > 0) "${movie.rating.ratingPercent}%" else "-",
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                )
            }
        }

        if (check) {
            if (loading) {
                FilmProgressIndicator(
                    size = 16.dp,
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = TraktTheme.colors.accent,
                    modifier = Modifier
                        .size(18.dp)
                        .onClickCombined(
                            onClick = onCheckClick ?: {},
                            onLongClick = onCheckLongClick,
                        ),
                )
            }
        }
    }
}

@Preview(widthDp = 300)
@Composable
private fun Preview() {
    TraktTheme {
        MovieMetaFooter(
            movie = PreviewData.movie1,
        )
    }
}

@Preview(widthDp = 300)
@Composable
private fun Preview2() {
    TraktTheme {
        MovieMetaFooter(
            movie = PreviewData.movie1.copy(
                released = nowLocalDay().plusDays(3),
            ),
            rating = false,
        )
    }
}

@Preview(widthDp = 300)
@Composable
private fun Preview3() {
    TraktTheme {
        MovieMetaFooter(
            movie = PreviewData.movie1.copy(
                released = nowLocalDay().minusDays(3),
            ),
            rating = false,
            check = true,
        )
    }
}

@Preview(widthDp = 300)
@Composable
private fun Preview4() {
    TraktTheme {
        MovieMetaFooter(
            movie = PreviewData.movie1.copy(
                released = nowLocalDay().minusDays(3),
            ),
            rating = false,
            check = true,
            loading = true,
            mediaIcon = true,
        )
    }
}
