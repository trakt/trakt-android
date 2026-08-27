package tv.trakt.trakt.core.episodes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SEPARATOR = "  •  "

@Composable
fun SeasonMetaFooter(
    season: Season,
    modifier: Modifier = Modifier,
    secondary: Boolean = false,
    loading: Boolean = false,
    rating: Boolean = true,
    userRating: UserRating? = null,
    check: Boolean = false,
    mediaIcon: Boolean = false,
    textStyle: TextStyle = TraktTheme.typography.cardSubtitle.copy(
        fontWeight = W500,
    ),
    onCheckClick: (() -> Unit)? = null,
    onCheckLongClick: (() -> Unit)? = null,
) {
    val epsString = stringResource(R.string.tag_text_number_of_episodes, season.episodeCount ?: 0)
    val metaString = remember {
        buildString {
            season.firstAired?.let {
                append(it.year)
            }
            if ((season.episodeCount ?: 0) > 0) {
                if (isNotEmpty()) append(SEPARATOR)
                append(epsString)
            }
        }
    }

    val isReleased = remember(season.firstAired) {
        season.firstAired?.isNowOrBefore() ?: false
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
        val textColor = when {
            secondary -> TraktTheme.colors.textSecondary
            else -> TraktTheme.colors.textPrimary
        }

        if (isReleased) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (mediaIcon) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shows_off),
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier
                            .size(14.dp)
                            .graphicsLayer {
                                translationY = -0.66.dp.toPx()
                            },
                    )
                }
                Text(
                    text = metaString,
                    color = textColor,
                    style = textStyle,
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar_upcoming),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = season.firstAired?.relativeDateTimeString() ?: "TBA",
                    color = textColor,
                    style = textStyle,
                )
            }
        }

        if (rating && !secondary && isReleased && !check) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(2.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_star_trakt_on),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = when {
                        season.rating.rating > 0 -> Purple400
                        else -> TraktTheme.colors.textSecondary
                    },
                )
                Text(
                    text = if (season.rating.rating > 0) "${season.rating.ratingPercent}%" else "-",
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                )
            }
        }

        if (userRating != null && userRating.rating > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(2.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_star_trakt_on),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = TraktTheme.colors.textPrimary,
                )
                Text(
                    text = userRating.rating5Scale,
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.meta.copy(fontSize = 12.sp),
                )
            }
        }

        if (check && !secondary) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(20.dp),
            ) {
                if (loading) {
                    FilmProgressIndicator(
                        size = 16.dp,
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_2),
                        contentDescription = null,
                        tint = TraktTheme.colors.accent,
                        modifier = Modifier
                            .size(20.dp)
                            .onClickCombined(
                                onClick = onCheckClick,
                                onLongClick = onCheckLongClick,
                            ),
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 300, locale = "us")
@Composable
private fun Preview() {
    TraktTheme {
        SeasonMetaFooter(
            season = PreviewData.season1,
        )
    }
}

@Preview(widthDp = 300, locale = "us")
@Composable
private fun Preview2() {
    TraktTheme {
        SeasonMetaFooter(
            season = PreviewData.season1.copy(
                firstAired = nowUtc().minusDays(3),
            ),
            rating = false,
        )
    }
}

@Preview(widthDp = 300, locale = "us")
@Composable
private fun Preview3() {
    TraktTheme {
        SeasonMetaFooter(
            season = PreviewData.season1.copy(
                firstAired = nowUtc().minusDays(3),
            ),
            rating = false,
            check = true,
        )
    }
}

@Preview(widthDp = 300, locale = "us")
@Composable
private fun Preview4() {
    TraktTheme {
        SeasonMetaFooter(
            season = PreviewData.season1.copy(
                firstAired = nowUtc().minusDays(3),
            ),
            rating = true,
            check = true,
            loading = true,
            mediaIcon = true,
        )
    }
}

@Preview(widthDp = 300, locale = "us")
@Composable
private fun Preview5() {
    TraktTheme {
        SeasonMetaFooter(
            season = PreviewData.season1.copy(
                firstAired = nowUtc().minusDays(3),
            ),
            rating = false,
            check = true,
            loading = true,
            mediaIcon = true,
            secondary = true,
        )
    }
}

@Preview(widthDp = 300, locale = "ar")
@Composable
private fun PreviewRtl() {
    TraktTheme {
        SeasonMetaFooter(
            season = PreviewData.season1,
            rating = true,
            check = true,
            mediaIcon = true,
        )
    }
}
