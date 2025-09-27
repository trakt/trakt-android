package tv.trakt.trakt.core.shows.ui

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.relativeDateTimeString
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.White
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SEPARATOR = "  •  "

@Composable
fun ShowMetaFooter(
    show: Show,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    rating: Boolean = true,
    check: Boolean = false,
    mediaIcon: Boolean = false,
    onCheckClick: (() -> Unit)? = null,
) {
    val epsString = stringResource(R.string.tag_text_number_of_episodes, show.airedEpisodes)
    val metaString = remember {
        buildString {
            show.released?.let {
                append(it.year)
            }
            if (show.airedEpisodes > 0) {
                if (isNotEmpty()) append(SEPARATOR)
                append(epsString)
            }
            if (!show.certification.isNullOrBlank()) {
                if (isNotEmpty()) append(SEPARATOR)
                append(show.certification)
            }
        }
    }

    val isReleased = remember(show.released) {
        show.released?.isNowOrBefore() ?: false
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
                        painter = painterResource(R.drawable.ic_shows_off),
                        contentDescription = null,
                        tint = TraktTheme.colors.textSecondary,
                        modifier = Modifier.Companion
                            .size(14.dp)
                            .graphicsLayer {
                                translationY = -1.dp.toPx()
                            },
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
                    text = show.released?.relativeDateTimeString() ?: "",
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
                    colorFilter = if (show.rating.rating > 0) whiteFilter else grayFilter,
                )
                Text(
                    text = if (show.rating.rating > 0) "${show.rating.ratingPercent}%" else "-",
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
        ShowMetaFooter(
            show = PreviewData.show1,
        )
    }
}

@Preview(widthDp = 300)
@Composable
private fun Preview2() {
    TraktTheme {
        ShowMetaFooter(
            show = PreviewData.show1.copy(
                released = nowUtc().minusDays(3),
            ),
            rating = false,
        )
    }
}

@Preview(widthDp = 300)
@Composable
private fun Preview3() {
    TraktTheme {
        ShowMetaFooter(
            show = PreviewData.show1.copy(
                released = nowUtc().minusDays(3),
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
        ShowMetaFooter(
            show = PreviewData.show1.copy(
                released = nowUtc().minusDays(3),
            ),
            rating = false,
            check = true,
            loading = true,
            mediaIcon = true,
        )
    }
}
