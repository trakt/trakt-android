package tv.trakt.trakt.core.summary.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsRatings(
    traktRatings: Int?,
    externalRatings: ExternalRating?,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
) {
    val grayFilter = remember {
        ColorFilter.colorMatrix(
            ColorMatrix().apply {
                setToSaturation(0F)
            },
        )
    }

    Row(
        horizontalArrangement = spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        val textStyle = TraktTheme.typography.meta.copy(fontSize = 12.sp)
        val iconSpace = spacedBy(4.dp, Alignment.Start)
        val emptyText = "— %"

        // Trakt Rating
        val traktRating = traktRatings ?: 0
        Row(
            horizontalArrangement = iconSpace,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_trakt_icon_color),
                contentDescription = null,
                modifier = Modifier.height(16.dp),
                colorFilter = if (traktRating > 0 && !hidden) null else grayFilter,
            )

            Box {
                Text(
                    text = when {
                        traktRating > 0 && !hidden -> "$traktRating%"
                        else -> emptyText
                    },
                    color = when {
                        traktRating > 0 && !hidden -> TraktTheme.colors.textPrimary
                        else -> TraktTheme.colors.textSecondary
                    },
                    style = textStyle,
                )
                Text(
                    text = "99%",
                    color = Color.Transparent,
                    style = textStyle,
                )
            }
        }

        // iMDB Rating
        val imdbRating = externalRatings?.imdb?.rating ?: 0F
        Crossfade(
            targetState = (imdbRating > 0 && !hidden),
            animationSpec = tween(delayMillis = 50),
        ) { hasRating ->
            Row(
                horizontalArrangement = iconSpace,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer {
                    translationX = (1.8).dp.toPx()
                },
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_imdb_color),
                    contentDescription = null,
                    modifier = Modifier.height(14.dp),
                    colorFilter = if (hasRating) null else grayFilter,
                )

                Box {
                    if (hasRating) {
                        Text(
                            text = externalRatings?.imdb?.ratingString ?: emptyText,
                            color = TraktTheme.colors.textPrimary,
                            style = textStyle,
                        )
                    } else {
                        Text(
                            text = emptyText,
                            color = TraktTheme.colors.textSecondary,
                            style = textStyle,
                        )
                    }

                    Text(
                        text = "0.0",
                        color = Color.Transparent,
                        style = textStyle,
                    )
                }
            }
        }

        // Rotten Tomatoes Rating
        val rottenRating = externalRatings?.rotten?.rating?.toInt() ?: 0
        Crossfade(
            targetState = (rottenRating > 0 && !hidden),
            animationSpec = tween(delayMillis = 50),
        ) { hasRating ->
            Row(
                horizontalArrangement = iconSpace,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = when {
                        hasRating -> painterResource(
                            externalRatings?.rotten?.ratingIcon ?: R.drawable.ic_rotten_tomato,
                        )
                        else -> painterResource(R.drawable.ic_rotten_tomato)
                    },
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    colorFilter = if (hasRating) null else grayFilter,
                )

                Box {
                    if (hasRating) {
                        Text(
                            text = "$rottenRating%",
                            color = TraktTheme.colors.textPrimary,
                            style = textStyle,
                        )
                    } else {
                        Text(
                            text = emptyText,
                            color = TraktTheme.colors.textSecondary,
                            style = textStyle,
                        )
                    }

                    Text(
                        text = "00%",
                        color = Color.Transparent,
                        style = textStyle,
                    )
                }
            }
        }

        // Rotten Tomatoes Audience Rating
        val rottenRatingAud = externalRatings?.rotten?.userRating ?: 0
        Crossfade(
            targetState = (rottenRatingAud > 0 && !hidden),
            animationSpec = tween(delayMillis = 50),
            modifier = Modifier.graphicsLayer {
                translationX = (-2).dp.toPx()
            },
        ) { hasRating ->
            Row(
                horizontalArrangement = iconSpace,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = when {
                        hasRating -> painterResource(
                            externalRatings?.rotten?.userRatingIcon ?: R.drawable.ic_rotten_audience_upright,
                        )
                        else -> painterResource(R.drawable.ic_rotten_audience_upright)
                    },
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    colorFilter = if (hasRating) null else grayFilter,
                )

                Box {
                    if (hasRating) {
                        Text(
                            text = "$rottenRatingAud%",
                            color = TraktTheme.colors.textPrimary,
                            style = textStyle,
                        )
                    } else {
                        Text(
                            text = emptyText,
                            color = TraktTheme.colors.textSecondary,
                            style = textStyle,
                        )
                    }

                    Text(
                        text = "00%",
                        color = Color.Transparent,
                        style = textStyle,
                    )
                }
            }
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(16.dp),
        ) {
            val ratings = ExternalRating(
                imdb = ExternalRating.ImdbRating(
                    rating = 8F,
                    votes = 123456,
                    link = "https://www.imdb.com/title/tt1234567/",
                ),
                meta = ExternalRating.MetaRating(
                    rating = 75,
                    link = "https://www.metacritic.com/movie/example",
                ),
                rotten = ExternalRating.RottenRating(
                    rating = 90F,
                    state = "certified",
                    userRating = 85,
                    userState = "upright",
                    link = "https://www.rottentomatoes.com/m/example",
                ),
            )

            DetailsRatings(
                traktRatings = 72,
                externalRatings = ratings,
            )

            DetailsRatings(
                hidden = true,
                traktRatings = 72,
                externalRatings = ratings,
            )
        }
    }
}
