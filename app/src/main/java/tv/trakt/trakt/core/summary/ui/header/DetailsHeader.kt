package tv.trakt.trakt.core.summary.ui.header

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.Config.webImdbPersonUrl
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.openExternalAppLink
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.ImdbId
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.summary.ui.DetailsRatings
import tv.trakt.trakt.core.summary.ui.header.poster.DetailsHeaderPoster
import tv.trakt.trakt.core.summary.ui.header.poster.DetailsHeaderPosterHorizontal
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Composable
internal fun DetailsHeader(
    modifier: Modifier = Modifier,
    titleHeader: @Composable (() -> Unit)? = null,
    titleFooter: @Composable (() -> Unit)? = null,
    title: String,
    genres: ImmutableList<String>,
    date: @Composable (() -> Unit)?,
    imageUrl: String?,
    imageHorizontal: Boolean,
    status: String?,
    certification: String?,
    runtime: Duration?,
    accentColor: Color?,
    traktRatings: Int?,
    externalRatings: ExternalRating?,
    externalRatingsVisible: Boolean,
    externalRottenVisible: Boolean,
    episodesCount: Int?,
    creditsCount: Int?,
    playsCount: Int?,
    personImdb: ImdbId? = null,
    loading: Boolean,
    extraRightColumn: @Composable (() -> Unit)? = null,
    onImdbClick: () -> Unit,
    onRottenClick: (link: String) -> Unit,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        if (!imageHorizontal) {
            DetailsHeaderPoster(
                imageUrl = imageUrl,
                accentColor = accentColor,
                loading = loading,
                creditsCount = creditsCount,
                playsCount = playsCount,
                personImdb = personImdb,
                onShareClick = onShareClick,
                onBackClick = onBackClick,
                extraRightColumn = extraRightColumn ?: {},
            )
        } else {
            DetailsHeaderPosterHorizontal(
                imageUrl = imageUrl,
                accentColor = accentColor,
                loading = loading,
                creditsCount = creditsCount,
                playsCount = playsCount,
                onShareClick = onShareClick,
                onBackClick = onBackClick,
                modifier = Modifier
                    .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
            )
        }

        if (externalRatingsVisible) {
            DetailsRatings(
                traktRatings = traktRatings,
                externalRatings = externalRatings,
                rottenEnabled = externalRottenVisible,
                onImdbClick = onImdbClick,
                onRottenClick = onRottenClick,
                modifier = Modifier
                    .padding(top = 20.dp),
            )
        } else {
            Spacer(Modifier.padding(top = 4.dp))
        }

        Column(
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 15.dp,
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                ),
        ) {
            val genresText = remember(genres) {
                genres.take(2).joinToString(" / ") { genre ->
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

            if (titleFooter != null) {
                titleFooter()
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(0.dp),
                modifier = Modifier
                    .padding(top = 5.dp),
            ) {
                date?.let {
                    date()
                    DotSeparator()
                }

                if (episodesCount != null && episodesCount > 0) {
                    Text(
                        text = stringResource(R.string.tag_text_number_of_episodes, episodesCount),
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.paragraphSmaller,
                        maxLines = 1,
                        overflow = Ellipsis,
                    )
                    DotSeparator()
                } else if (runtime != null) {
                    Text(
                        text = runtime.inWholeMinutes.durationFormat(),
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.paragraphSmaller,
                        maxLines = 1,
                        overflow = Ellipsis,
                    )
                    DotSeparator()
                }

                if (!certification.isNullOrBlank()) {
                    Text(
                        text = certification,
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.paragraphSmaller,
                        maxLines = 1,
                        overflow = Ellipsis,
                    )
                    DotSeparator()
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
                        .padding(top = 4.dp),
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
internal fun PosterChipsGroup(
    creditsCount: Int?,
    playsCount: Int?,
    personImdb: ImdbId?,
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
                    playsCount > 1 -> "${stringResource(R.string.tag_text_watched)} • $playsCount"
                    else -> stringResource(R.string.tag_text_watched)
                },
                icon = painterResource(R.drawable.ic_check_double),
            )
        }

        if (personImdb != null) {
            val context = LocalContext.current
            Image(
                painter = painterResource(R.drawable.ic_imdb_color),
                contentDescription = null,
                modifier = Modifier
                    .height(22.dp)
                    .graphicsLayer {
                        translationY = 1.dp.toPx()
                    }
                    .onClick {
                        openExternalAppLink(
                            context = context,
                            packageId = "com.imdb.mobile",
                            packageName = "imdb",
                            uri = webImdbPersonUrl(personImdb.value).toUri(),
                        )
                    },
            )
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
                horizontal = 8.dp,
                vertical = 4.dp,
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

@Composable
private fun DotSeparator() {
    Text(
        text = "  •  ",
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.paragraphSmaller,
        modifier = Modifier
            .padding(horizontal = 1.dp),
    )
}

// Previews

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
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
            titleFooter = {
                Text(
                    text = "Created by John Doe",
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.paragraphSmaller,
                )
            },
            extraRightColumn = {
                Column(
                    horizontalAlignment = CenterHorizontally,
                    verticalArrangement = spacedBy(16.dp),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_instagram),
                        tint = TraktTheme.colors.textPrimary,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_x_twitter),
                        tint = TraktTheme.colors.textPrimary,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                    )
                }
            },
            genres = listOf("Action", "Adventure", "Sci-Fi").toImmutableList(),
            date = null,
            imageUrl = null,
            imageHorizontal = false,
            status = "Released",
            certification = "PG-13",
            accentColor = null,
            traktRatings = 72,
            episodesCount = 23,
            playsCount = 0,
            creditsCount = 0,
            runtime = null,
            loading = false,
            externalRatingsVisible = true,
            externalRottenVisible = true,
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
            onBackClick = {},
            onImdbClick = {},
            onRottenClick = {},
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        DetailsHeader(
            title = "Episode 4",
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
            runtime = 45.minutes,
            imageUrl = null,
            imageHorizontal = true,
            status = "Released",
            certification = "PG-13",
            accentColor = null,
            traktRatings = 72,
            playsCount = 0,
            creditsCount = 0,
            episodesCount = null,
            loading = false,
            externalRatingsVisible = true,
            externalRottenVisible = true,
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
            onBackClick = {},
            onImdbClick = {},
            onRottenClick = {},
        )
    }
}
