@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.profile.sections.thismonth

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import tv.trakt.trakt.common.Config.webMonthReviewUrl
import tv.trakt.trakt.common.Config.webYearReviewUrl
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.common.ui.theme.colors.Shade920
import tv.trakt.trakt.common.ui.theme.colors.Shade940
import tv.trakt.trakt.core.profile.sections.thismonth.model.ThisMonthStats
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun ThisMonthCard(
    user: User,
    modifier: Modifier = Modifier,
    containerColor: Color = Shade920,
    containerImage: String? = null,
    stats: ThisMonthStats?,
) {
    val uriHandler = LocalUriHandler.current

    val currentDate = remember { nowLocal() }
    val previousMonth = remember {
        currentDate.minusMonths(1)
    }

    val colorGradient = remember {
        verticalGradient(
            colors = listOf(
                Color.Transparent,
                Red500.copy(alpha = 0.5F),
            ),
        )
    }

    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .shadow(4.dp, shape)
            .clip(shape)
            .background(containerColor),
    ) {
        containerImage?.let {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(containerImage)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .alpha(0.1F)
                    .matchParentSize(),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35F)
                    .align(Alignment.BottomCenter)
                    .background(colorGradient),
            )
        }

        Column(
            verticalArrangement = spacedBy(20.dp),
            modifier = Modifier.padding(vertical = 16.dp),
        ) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar_trakt),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.text_this_month).uppercase(),
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.heading6,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            translationY = 0.25.dp.toPx()
                        },
                    )
                }

                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = spacedBy(8.dp),
                    modifier = Modifier.onClick {
                        uriHandler.openUri(
                            webYearReviewUrl(
                                user = user.ids.slug.value,
                                year = currentDate.year,
                            ),
                        )
                    },
                ) {
                    Text(
                        text = currentDate.year.toString(),
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.heading6,
                        textAlign = TextAlign.Center,
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_external),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .size(16.dp),
                    )
                }
            }

            Row(
                horizontalArrangement = spacedBy(6.dp),
                modifier = Modifier
                    .horizontalScroll(
                        state = rememberScrollState(),
                        overscrollEffect = null,
                    )
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 2.dp),
            ) {
                StatsChip(
                    text = stringResource(R.string.text_episodes_watched, stats?.episodesCount ?: 0),
                    icon = painterResource(R.drawable.ic_shows_off),
                )
                StatsChip(
                    text = stringResource(R.string.text_shows_watched, stats?.showsCount ?: 0),
                    icon = painterResource(R.drawable.ic_shows_off),
                )
                StatsChip(
                    text = stringResource(R.string.text_movies_watched, stats?.moviesCount ?: 0),
                    icon = painterResource(R.drawable.ic_movies_off),
                )
            }

            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(8.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .onClick {
                        uriHandler.openUri(
                            webMonthReviewUrl(
                                user = user.ids.slug.value,
                                month = previousMonth.monthValue,
                                year = previousMonth.year,
                            ),
                        )
                    },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_external),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(16.dp),
                )
                Text(
                    text = previousMonth.month.getDisplayName(
                        TextStyle.FULL,
                        Locale.US,
                    ).uppercase(),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading6,
                )
            }
        }
    }
}

@Composable
private fun StatsChip(
    modifier: Modifier = Modifier,
    text: String,
    icon: Painter,
) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(6.dp),
        modifier = modifier
            .animateContentSize()
            .shadow(1.dp, shape)
            .background(
                color = Shade940,
                shape = shape,
            )
            .padding(
                horizontal = 8.dp,
                vertical = 6.dp,
            ),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(16.dp),
        )
        Text(
            text = text.uppercase(),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.buttonTertiary,
        )
    }
}

@Preview(widthDp = 350)
@Composable
private fun Preview() {
    TraktTheme {
        ThisMonthCard(
            user = PreviewData.user1,
            modifier = Modifier.padding(16.dp),
            stats = ThisMonthStats(
                showsCount = 12,
                moviesCount = 0,
                episodesCount = 34,
            ),
        )
    }
}
