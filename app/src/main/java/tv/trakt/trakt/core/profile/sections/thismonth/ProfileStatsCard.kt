@file:OptIn(ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.profile.sections.thismonth

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.Config.webMonthReviewUrl
import tv.trakt.trakt.common.Config.webYearReviewUrl
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.ui.theme.colors.Red600
import tv.trakt.trakt.common.ui.theme.colors.Shade920
import tv.trakt.trakt.core.profile.sections.thismonth.model.ProfileStats
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun ProfileStatsCard(
    user: User,
    modifier: Modifier = Modifier,
    containerColor: Color = Shade920,
    containerImage: String? = null,
    loading: Boolean = false,
    showAllStats: Boolean = true,
    stats: ProfileStats?,
) {
    val uriHandler = LocalUriHandler.current
    val currentDate = remember { nowLocal() }
    val previousMonth = remember { currentDate.minusMonths(1) }

    val colorGradient = remember {
        verticalGradient(
            0F to Color.Transparent,
            0.7F to Color.Transparent,
            1F to Red600.copy(alpha = 0.4F),
        )
    }

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = {
        when {
            showAllStats -> 2
            else -> 1
        }
    })

    Box(
        modifier = modifier
            .shadow(4.dp, DefaultCardShape)
            .clip(DefaultCardShape)
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
                    .matchParentSize()
                    .background(colorGradient),
            )
        }

        Column(
            verticalArrangement = spacedBy(20.dp),
            modifier = Modifier.padding(vertical = 16.dp),
        ) {
            HeaderRow(
                pagerState = pagerState,
                currentYear = currentDate.year,
                onLabelClick = {
                    togglePage(
                        scope = scope,
                        pagerState = pagerState,
                    )
                },
                onYearClick = {
                    uriHandler.openUri(
                        webYearReviewUrl(
                            user = user.ids.slug.value,
                            year = currentDate.year,
                        ),
                    )
                },
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                val showAll = showAllStats && page == 1

                StatsChipsRow(
                    episodes = (if (showAll) stats?.allEpisodesCount else stats?.episodesCount) ?: 0,
                    shows = (if (showAll) stats?.allShowsCount else stats?.showsCount) ?: 0,
                    movies = (if (showAll) stats?.allMoviesCount else stats?.moviesCount) ?: 0,
                    loading = loading,
                )
            }

            PreviousMonthRow(
                visible = pagerState.currentPage == 0,
                previousMonth = previousMonth,
                onClick = {
                    uriHandler.openUri(
                        webMonthReviewUrl(
                            user = user.ids.slug.value,
                            month = previousMonth.monthValue,
                            year = previousMonth.year,
                        ),
                    )
                },
            )
        }

        if (showAllStats) {
            PagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 18.dp, vertical = 18.dp)
                    .onClick(throttle = false) {
                        togglePage(
                            scope = scope,
                            pagerState = pagerState,
                        )
                    },
            )
        }
    }
}

@Composable
private fun HeaderRow(
    pagerState: PagerState,
    currentYear: Int,
    onLabelClick: () -> Unit,
    onYearClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Crossfade(
            targetState = pagerState.targetPage,
            label = "ProfileStatsTitle",
        ) { page ->
            val showAll = page == 1

            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(6.dp),
                modifier = Modifier
                    .onClick(throttle = false) {
                        onLabelClick()
                    },
            ) {
                Icon(
                    painter = painterResource(
                        if (showAll) R.drawable.ic_history else R.drawable.ic_calendar,
                    ),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(
                        if (showAll) R.string.text_all_time else R.string.text_this_month,
                    ).uppercase(),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading6,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        translationY = 0.25.dp.toPx()
                    },
                )
            }
        }

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(8.dp),
            modifier = Modifier.onClick { onYearClick() },
        ) {
            Text(
                text = currentYear.toString(),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading6,
                textAlign = TextAlign.Center,
            )
            Icon(
                painter = painterResource(R.drawable.ic_external),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun PreviousMonthRow(
    visible: Boolean,
    previousMonth: ZonedDateTime,
    onClick: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val appLocale = remember(configuration) {
        AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0F,
        label = "alpha",
    )

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(6.dp),
        modifier = Modifier
            .alpha(animatedAlpha)
            .padding(horizontal = 16.dp)
            .onClick(enabled = visible) {
                onClick()
            },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_history),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = previousMonth.month.getDisplayName(
                TextStyle.FULL_STANDALONE,
                appLocale,
            ).uppercase(),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading6,
        )
    }
}

@Composable
private fun StatsChipsRow(
    episodes: Int,
    shows: Int,
    movies: Int,
    loading: Boolean,
) {
    val episodesText = rememberThousandsFormat(episodes)
    val showsText = rememberThousandsFormat(shows)
    val moviesText = rememberThousandsFormat(movies)
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
            text = stringResource(R.string.text_episodes_watched, episodesText),
            icon = painterResource(R.drawable.ic_shows_off),
            loading = loading,
        )
        StatsChip(
            text = stringResource(R.string.text_shows_watched, showsText),
            icon = painterResource(R.drawable.ic_shows_off),
            loading = loading,
        )
        StatsChip(
            text = stringResource(R.string.text_movies_watched, moviesText),
            icon = painterResource(R.drawable.ic_movies_off),
            loading = loading,
        )
    }
}

@Composable
private fun PagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(6.dp),
        modifier = modifier,
    ) {
        repeat(pagerState.pageCount) { i ->
            val selected = pagerState.currentPage == i
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        TraktTheme.colors.textPrimary.copy(
                            alpha = if (selected) 1F else 0.35F,
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun StatsChip(
    modifier: Modifier = Modifier,
    text: String,
    icon: Painter,
    loading: Boolean,
) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(6.dp),
        modifier = modifier
            .animateContentSize()
            .shadow(1.dp, shape)
            .background(
                color = Shade920,
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

        if (loading) {
            Row {
                Spacer(
                    modifier = Modifier.width(56.dp),
                )
            }
        } else {
            Text(
                text = text.uppercase(),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonTertiary,
            )
        }
    }
}

private fun togglePage(
    scope: CoroutineScope,
    pagerState: PagerState,
) {
    scope.launch {
        pagerState.animateScrollToPage(
            when (pagerState.currentPage) {
                0 -> 1
                else -> 0
            },
        )
    }
}

@Preview(widthDp = 350)
@Composable
private fun Preview() {
    TraktTheme {
        ProfileStatsCard(
            user = PreviewData.user1,
            modifier = Modifier.padding(16.dp),
            stats = ProfileStats(
                showsCount = 12,
                moviesCount = 0,
                episodesCount = 34,
                allShowsCount = 87,
                allMoviesCount = 145,
                allEpisodesCount = 2310,
            ),
        )
    }
}

@Preview(widthDp = 350)
@Composable
private fun Preview2() {
    TraktTheme {
        ProfileStatsCard(
            user = PreviewData.user1,
            modifier = Modifier.padding(16.dp),
            loading = true,
            stats = ProfileStats(
                showsCount = 12,
                moviesCount = 0,
                episodesCount = 34,
            ),
        )
    }
}
