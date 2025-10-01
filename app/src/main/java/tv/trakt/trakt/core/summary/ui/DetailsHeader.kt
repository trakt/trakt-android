package tv.trakt.trakt.core.summary.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.ui.theme.colors.Purple300
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsHeader(
    movie: Movie,
    ratings: ExternalRating?,
    onShareClick: () -> Unit,
    onTrailerClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailsHeader(
        title = movie.title,
        status = movie.status,
        year = movie.released?.year ?: movie.year,
        genres = movie.genres,
        images = movie.images,
        trailer = movie.trailer?.toUri(),
        accentColor = movie.colors?.colors?.first,
        credits = movie.credits,
        traktRatings = movie.rating.ratingPercent,
        ratings = ratings,
        watchCount = null,
        onBackClick = onBackClick,
        onTrailerClick = onTrailerClick,
        onShareClick = onShareClick,
        modifier = modifier,
    )
}

@Composable
private fun DetailsHeader(
    title: String,
    genres: List<String>,
    year: Int?,
    images: Images?,
    status: String?,
    trailer: Uri?,
    accentColor: Color?,
    traktRatings: Int?,
    ratings: ExternalRating?,
    credits: Int?,
    watchCount: Int?,
    onShareClick: () -> Unit,
    onTrailerClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Box(
            contentAlignment = TopCenter,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val posterSpace = 64.dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(posterSpace)
                    .padding(top = 8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .onClick(onClick = onBackClick),
                )
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = posterSpace),
            ) {
                DetailsPoster(
                    imageUrl = images?.getPosterUrl(Images.Size.MEDIUM),
                    color = accentColor,
                    modifier = Modifier
                        .fillMaxWidth(),
                )

                Row(
                    horizontalArrangement = spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            translationY = 11.dp.toPx()
                        },
                ) {
                    if (credits != null && credits > 0) {
                        PosterChip(
                            text = "${stringResource(R.string.header_post_credits)} • $credits".uppercase(),
                            modifier = Modifier,
                        )
                    }

                    if (watchCount != null && watchCount > 0) {
                        PosterChip(
                            text = watchCount.toString(),
                            icon = painterResource(R.drawable.ic_check_round),
                            modifier = Modifier,
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(24.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(posterSpace)
                    .padding(top = 8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_share),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .onClick(onClick = onShareClick),
                )

                Icon(
                    painter = painterResource(R.drawable.ic_trailer),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier
                        .alpha(if (trailer != null) 1F else 0.25F)
                        .size(21.dp)
                        .onClick(
                            enabled = trailer != null,
                            onClick = onTrailerClick,
                        ),
                )
            }
        }

        DetailsRatings(
            traktRatings = traktRatings,
            externalRatings = ratings,
            modifier = Modifier
                .padding(
                    top = when {
                        (credits != null && credits > 0) -> 26.dp
                        (watchCount != null && watchCount > 0) -> 26.dp
                        else -> 18.dp
                    },
                ),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 16.dp,
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                ),
        ) {
            val genresText = remember(genres) {
                genres.take(3).joinToString(", ") { genre ->
                    genre.replaceFirstChar {
                        it.uppercaseChar()
                    }
                }
            }

            Text(
                text = title,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading2,
                maxLines = 1,
                overflow = Ellipsis,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = TraktTheme.typography.heading2.fontSize,
                    minFontSize = 16.sp,
                    stepSize = 2.sp,
                ),
            )

            Text(
                text = "$year  •  $genresText",
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
                maxLines = 1,
                overflow = Ellipsis,
                modifier = Modifier
                    .padding(top = 5.dp),
            )

            status?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(4.dp),
                    modifier = Modifier
                        .padding(top = 2.dp),
                ) {
                    Text(
                        text = it.uppercase(),
                        color = when (it.lowercase()) {
                            "canceled", "ended" -> Red500
                            else -> Purple300
                        },
                        style = TraktTheme.typography.meta,
                        modifier = Modifier
                            .padding(top = 1.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PosterChip(
    modifier: Modifier = Modifier,
    text: String,
    icon: Painter? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(5.dp),
        modifier = modifier
            .background(Color.White, RoundedCornerShape(100))
            .padding(
                horizontal = 9.dp,
                vertical = 5.dp,
            ),
    ) {
        icon?.let {
            Icon(
                painter = it,
                tint = Color.Black,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            text = text,
            color = Color.Black,
            style = TraktTheme.typography.meta,
        )
    }
}

@Preview
@Composable
private fun DetailsHeaderPreview() {
    TraktTheme {
        DetailsHeader(
            title = "Movie Title",
            genres = listOf("Action", "Adventure", "Sci-Fi"),
            year = 2023,
            images = null,
            status = "Released",
            trailer = null,
            accentColor = null,
            traktRatings = 72,
            credits = 1,
            watchCount = 2,
            ratings = ExternalRating(
                imdb = ExternalRating.ImdbRating(
                    rating = 7.5F,
                    votes = 12345,
                    link = "https://www.imdb.com/title/tt1234567/",
                ),
                meta = ExternalRating.MetaRating(
                    rating = 75,
                    link = "https://www.metacritic.com/movie/sample-movie",
                ),
                rotten = ExternalRating.RottenRating(
                    rating = 85F,
                    state = "fresh",
                    userRating = 90,
                    userState = "upright",
                    link = "https://www.rottentomatoes.com/m/sample_movie",
                ),
            ),
            onShareClick = {},
            onTrailerClick = {},
            onBackClick = {},
        )
    }
}
