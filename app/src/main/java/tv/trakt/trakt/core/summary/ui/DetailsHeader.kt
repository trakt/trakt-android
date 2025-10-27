package tv.trakt.trakt.core.summary.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.common.ui.theme.colors.Shade700
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsHeader(
    movie: Movie,
    ratings: ExternalRating?,
    playsCount: Int?,
    creditsCount: Int?,
    loading: Boolean,
    onShareClick: () -> Unit,
    onTrailerClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isReleased = remember {
        movie.released?.isTodayOrBefore() ?: false
    }

    DetailsHeader(
        title = movie.title,
        status = movie.status,
        date = {
            Text(
                text = when {
                    isReleased -> (movie.released?.year ?: movie.year).toString()
                    else -> movie.released?.format(mediumDateFormat) ?: movie.year.toString()
                },
                color = when {
                    isReleased -> TraktTheme.colors.textSecondary
                    else -> TraktTheme.colors.textPrimary
                },
                style = when {
                    isReleased -> TraktTheme.typography.paragraphSmaller
                    else -> TraktTheme.typography.paragraphSmaller.copy(fontWeight = W700)
                },
                maxLines = 1,
                modifier = Modifier.padding(
                    end = if (!isReleased) 1.dp else 0.dp,
                ),
            )
        },
        genres = movie.genres,
        imageUrl = movie.images?.getPosterUrl(Size.MEDIUM),
        trailer = movie.trailer?.toUri(),
        accentColor = movie.colors?.colors?.first,
        traktRatings = when {
            isReleased -> movie.rating.ratingPercent
            else -> null
        },
        externalRatings = ratings,
        playsCount = playsCount,
        creditsCount = creditsCount,
        certification = movie.certification,
        loading = loading,
        onBackClick = onBackClick,
        onTrailerClick = onTrailerClick,
        onShareClick = onShareClick,
        modifier = modifier,
    )
}

@Composable
internal fun DetailsHeader(
    show: Show,
    ratings: ExternalRating?,
    airedCount: Int,
    playsCount: Int,
    loading: Boolean,
    onShareClick: () -> Unit,
    onTrailerClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isReleased = remember {
        show.released?.isNowOrBefore() ?: false
    }

    val playsCount = remember(airedCount, playsCount) {
        if (airedCount == 0) {
            return@remember 0
        }
        playsCount / airedCount
    }

    DetailsHeader(
        title = show.title,
        status = show.status,
        date = {
            Text(
                text = when {
                    isReleased -> (show.released?.year ?: show.year).toString()
                    else -> show.released?.format(mediumDateFormat) ?: show.year.toString()
                },
                color = when {
                    isReleased -> TraktTheme.colors.textSecondary
                    else -> TraktTheme.colors.textPrimary
                },
                style = when {
                    isReleased -> TraktTheme.typography.paragraphSmaller
                    else -> TraktTheme.typography.paragraphSmaller.copy(fontWeight = W700)
                },
                maxLines = 1,
                modifier = Modifier.padding(
                    end = if (!isReleased) 1.dp else 0.dp,
                ),
            )
        },
        genres = show.genres,
        imageUrl = show.images?.getPosterUrl(Size.MEDIUM),
        trailer = show.trailer?.toUri(),
        accentColor = show.colors?.colors?.first,
        traktRatings = when {
            isReleased -> show.rating.ratingPercent
            else -> null
        },
        externalRatings = ratings,
        playsCount = playsCount,
        creditsCount = null,
        certification = show.certification,
        loading = loading,
        onBackClick = onBackClick,
        onTrailerClick = onTrailerClick,
        onShareClick = onShareClick,
        modifier = modifier,
    )
}

@Composable
internal fun DetailsHeader(
    episode: Episode,
    show: Show,
    ratings: ExternalRating?,
    playsCount: Int?,
    loading: Boolean,
    onShowClick: (Show) -> Unit,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isReleased = remember {
        episode.firstAired?.isNowOrBefore() ?: false
    }

    DetailsHeader(
        title = episode.title,
        titleHeader = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(4.dp),
                modifier = Modifier.padding(
                    top = 4.dp,
                    bottom = 3.dp,
                ),
            ) {
                val font = TraktTheme.typography.heading6.copy(fontSize = 13.sp)
                Text(
                    text = show.title.uppercase(),
                    color = TraktTheme.colors.textPrimary,
                    style = font.copy(fontSize = 13.sp),
                    maxLines = 1,
                    overflow = Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = font.fontSize,
                        minFontSize = 10.sp,
                        stepSize = 1.sp,
                    ),
                    modifier = Modifier
                        .onClick(onClick = { onShowClick(show) }),
                )

                Text(
                    text = "/",
                    color = TraktTheme.colors.textSecondary,
                    style = font.copy(fontSize = 13.sp),
                    maxLines = 1,
                    overflow = Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = font.fontSize,
                        minFontSize = 10.sp,
                        stepSize = 1.sp,
                    ),
                )

                Text(
                    text = episode.seasonEpisode.toDisplayString(),
                    color = TraktTheme.colors.textPrimary,
                    style = font.copy(fontSize = 13.sp),
                    maxLines = 1,
                    overflow = Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = font.fontSize,
                        minFontSize = 10.sp,
                        stepSize = 1.sp,
                    ),
                )
            }
        },
        status = show.status,
        certification = show.certification,
        date = {
            Text(
                text = when {
                    isReleased -> (episode.firstAired?.year ?: show.year).toString()
                    else -> episode.firstAired?.format(mediumDateFormat) ?: show.year.toString()
                },
                color = when {
                    isReleased -> TraktTheme.colors.textSecondary
                    else -> TraktTheme.colors.textPrimary
                },
                style = when {
                    isReleased -> TraktTheme.typography.paragraphSmaller
                    else -> TraktTheme.typography.paragraphSmaller.copy(fontWeight = W700)
                },
                maxLines = 1,
                modifier = Modifier.padding(
                    end = if (!isReleased) 1.dp else 0.dp,
                ),
            )
        },
        genres = show.genres,
        imageUrl = show.images?.getPosterUrl(Size.MEDIUM),
        trailer = show.trailer?.toUri(),
        accentColor = show.colors?.colors?.first,
        traktRatings = when {
            isReleased -> episode.rating.ratingPercent
            else -> null
        },
        externalRatings = ratings,
        playsCount = playsCount,
        creditsCount = 0,
        loading = loading,
        onBackClick = onBackClick,
        onTrailerClick = null,
        onShareClick = onShareClick,
        modifier = modifier,
    )
}

@Composable
internal fun DetailsHeader(
    person: Person,
    loading: Boolean,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailsHeader(
        loading = loading,
        title = person.name,
        genres = EmptyImmutableList,
        imageUrl = person.images?.getHeadshotUrl(),
        onShareClick = onShareClick,
        onBackClick = onBackClick,
        date = null,
        status = null,
        externalRatings = null,
        externalRatingsVisible = false,
        trailer = null,
        playsCount = null,
        certification = null,
        accentColor = Shade700,
        creditsCount = null,
        traktRatings = null,
        onTrailerClick = null,
        modifier = modifier,
    )
}

@Composable
private fun DetailsHeader(
    modifier: Modifier = Modifier,
    titleHeader: @Composable (() -> Unit)? = null,
    title: String,
    genres: ImmutableList<String>,
    date: @Composable (() -> Unit)?,
    imageUrl: String?,
    status: String?,
    certification: String?,
    trailer: Uri?,
    accentColor: Color?,
    traktRatings: Int?,
    externalRatings: ExternalRating?,
    externalRatingsVisible: Boolean = true,
    creditsCount: Int?,
    playsCount: Int?,
    loading: Boolean,
    onShareClick: () -> Unit,
    onTrailerClick: (() -> Unit)?,
    onBackClick: () -> Unit,
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
                    imageUrl = imageUrl,
                    color = accentColor,
                    modifier = Modifier
                        .fillMaxWidth(),
                )

                this@Column.AnimatedVisibility(
                    visible = !loading,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(150)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            translationY = 6.dp.toPx()
                        },
                ) {
                    Row(
                        horizontalArrangement = spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (creditsCount != null && creditsCount > 0) {
                            PosterChip(
                                text = "${stringResource(R.string.header_post_credits)} • $creditsCount".uppercase(),
                            )
                        }

                        if (playsCount != null && playsCount > 0) {
                            PosterChip(
                                text = when {
                                    playsCount > 1 -> stringResource(R.string.text_watched_count, playsCount)
                                    else -> stringResource(R.string.text_watched)
                                },
                                icon = painterResource(R.drawable.ic_check_round),
                            )
                        }
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

                if (onTrailerClick != null) {
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
        }

        if (externalRatingsVisible) {
            DetailsRatings(
                traktRatings = traktRatings,
                externalRatings = externalRatings,
                modifier = Modifier
                    .padding(top = 20.dp),
            )
        } else {
            Spacer(Modifier.padding(top = 4.dp))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 15.dp,
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                ),
        ) {
            val genresText = remember(genres) {
                genres.take(3).joinToString(" / ") { genre ->
                    genre.replaceFirstChar {
                        it.uppercaseChar()
                    }
                }
            }

            if (titleHeader != null) {
                titleHeader()
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(0.dp),
                modifier = Modifier
                    .padding(top = 5.dp),
            ) {
                date?.let {
                    date()
                    Text(
                        text = "  •  ",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.paragraphSmaller,
                        modifier = Modifier
                            .padding(horizontal = 1.dp),
                    )
                }

                if (!certification.isNullOrBlank()) {
                    Text(
                        text = certification,
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.paragraphSmaller,
                        maxLines = 1,
                        overflow = Ellipsis,
                    )
                    Text(
                        text = "  •  ",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.paragraphSmaller,
                        modifier = Modifier
                            .padding(horizontal = 1.dp),
                    )
                }

                Text(
                    text = genresText,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmaller,
                    maxLines = 1,
                    overflow = Ellipsis,
                )
            }

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
                            "canceled" -> Red500
                            "ended" -> TraktTheme.colors.detailsStatus2
                            else -> TraktTheme.colors.detailsStatus1
                        },
                        style = TraktTheme.typography.meta,
                        modifier = Modifier.padding(top = 1.dp),
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
        horizontalArrangement = spacedBy(6.dp),
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
        if (text.isNotBlank()) {
            Text(
                text = text.uppercase(),
                color = Color.Black,
                style = TraktTheme.typography.meta,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        DetailsHeader(
            title = "Movie Title",
            titleHeader = {
                Text(
                    text = "Some Title Header".uppercase(),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading6.copy(fontSize = 13.sp),
                    maxLines = 1,
                    overflow = Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = 13.sp,
                        minFontSize = 10.sp,
                        stepSize = 1.sp,
                    ),
                )
            },
            genres = listOf("Action", "Adventure", "Sci-Fi").toImmutableList(),
            date = null,
            imageUrl = null,
            status = "Released",
            trailer = null,
            certification = "PG-13",
            accentColor = null,
            traktRatings = 72,
            playsCount = 2,
            creditsCount = 2,
            loading = true,
            externalRatings = ExternalRating(
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
