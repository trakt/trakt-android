package tv.trakt.trakt.core.movies.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.relativeDateString
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.White
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
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (mediaIcon) {
                    Icon(
                        painter = painterResource(R.drawable.ic_movies_off),
                        contentDescription = null,
                        tint = TraktTheme.colors.textSecondary,
                        modifier = Modifier.Companion.size(14.dp),
                    )
                }
                Text(
                    text = metaString,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
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
                    tint = TraktTheme.colors.textSecondary,
                    modifier = Modifier.Companion.size(14.dp),
                )
                Text(
                    text = movie.released?.relativeDateString() ?: "",
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
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
                val whiteFilter = remember {
                    ColorFilter.tint(White)
                }

                Image(
                    painter = painterResource(R.drawable.ic_trakt_icon),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    colorFilter = if (movie.rating.rating > 0) whiteFilter else grayFilter,
                )
                Text(
                    text = if (movie.rating.rating > 0) "${movie.rating.ratingPercent}%" else "-",
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                )
            }
        }

        if (check) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(TraktTheme.colors.chipContainerOnContent, RoundedCornerShape(10.dp))
                    .size(30.dp),
            ) {
                if (loading) {
                    FilmProgressIndicator(size = 18.dp)
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_round),
                        contentDescription = null,
                        tint = TraktTheme.colors.accent,
                        modifier = Modifier
                            .size(19.dp)
                            .onClick(onClick = onCheckClick ?: {}),
                    )
                }
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
                released = nowLocalDay().minusDays(3),
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
