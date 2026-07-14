package tv.trakt.trakt.core.home.sections.streaks

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Shade700
import tv.trakt.trakt.core.home.sections.streaks.model.MonthlyStreakData
import tv.trakt.trakt.core.home.sections.streaks.model.MonthlyStreakData.StreakDataPoint
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.DayOfWeek
import java.time.LocalDate

private val viewHeight = 72.dp
private val viewShape = RoundedCornerShape(24.dp)

@Composable
internal fun HomeStreaksView(
    modifier: Modifier = Modifier,
    viewModel: HomeStreaksViewModel = koinViewModel(),
    onClick: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeStreaksContent(
        data = state.data,
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
private fun HomeStreaksContent(
    data: MonthlyStreakData?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Crossfade(
        targetState = data != null,
        animationSpec = tween(200),
    ) { hasData ->
        if (hasData && data != null) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier
                    .fillMaxWidth()
                    .height(viewHeight)
                    .shadow(2.dp, viewShape)
                    .background(TraktTheme.colors.dialogContainer, viewShape)
                    .padding(start = 8.dp, end = 8.dp)
                    .onClick(onClick = onClick),
            ) {
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                ) {
                    Image(
                        painter = painterResource(
                            when {
                                data.currentStreakTotal <= 1 -> R.drawable.ic_flame_1
                                data.currentStreakTotal <= 7 -> R.drawable.ic_flame_2
                                else -> R.drawable.ic_flame_3
                            },
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp),
                    )

                    Text(
                        maxLines = 1,
                        style = TraktTheme.typography.heading5,
                        color = TraktTheme.colors.textPrimary,
                        text = "${
                            stringResource(
                                when {
                                    data.currentStreakTotal > 1 -> R.string.text_stats_days_count
                                    else -> R.string.text_stats_day_count
                                },
                                data.currentStreakTotal,
                            )
                        } ${stringResource(R.string.text_stats_watching_streak)}",
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    WeekActivityPills(activity = data.activity)
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        tint = TraktTheme.colors.textSecondary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        } else {
            StreaksHomeViewSkeleton(
                modifier = modifier
                    .fillMaxWidth()
                    .height(viewHeight)
                    .shadow(2.dp, viewShape),
            )
        }
    }
}

@Composable
private fun WeekActivityPills(
    activity: ImmutableMap<LocalDate, StreakDataPoint>,
    modifier: Modifier = Modifier,
) {
    val today = nowLocalDay()
    val startOfWeek = today.with(DayOfWeek.MONDAY)
    val days = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = CenterVertically,
        modifier = modifier,
    ) {
        days.forEach { date ->
            val count = activity[date]?.total ?: 0

            val background = when {
                !date.isTodayOrBefore() -> Color.Transparent
                date <= today && count > 0 -> Purple500
                date < today && count == 0 -> Shade700
                else -> Color.Transparent
            }

            val border = when {
                !date.isTodayOrBefore() -> Shade700
                count > 0 || today == date -> Purple500
                else -> Color.Transparent
            }

            val width = 10.dp
            val height = 22.dp

            Box(
                modifier = Modifier
                    .width(width)
                    .height(height)
                    .border(
                        0.5.dp,
                        if (date == today) Color.White else Color.Transparent,
                        RoundedCornerShape(100),
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .width(width)
                        .height(height)
                        .border(
                            0.5.dp,
                            border,
                            RoundedCornerShape(100),
                        )
                        .background(background, RoundedCornerShape(100)),
                )
            }
        }
    }
}

@Composable
private fun StreaksHomeViewSkeleton(modifier: Modifier = Modifier) {
    val containerColor = TraktTheme.colors.skeletonContainer
    val shimmerColor = TraktTheme.colors.skeletonShimmer

    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition
        .animateColor(
            initialValue = containerColor,
            targetValue = shimmerColor,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    Box(
        modifier = modifier.background(shimmerTransition, viewShape),
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
)
@Composable
private fun Preview() {
    TraktTheme {
        HomeStreaksContent(
            data = MonthlyStreakData(
                activity = persistentMapOf(),
                currentStreakTotal = 333,
                currentStreak = 33,
                previousStreak = 2,
                previousStreakTotal = 0,
                droppedStreaks = 2,
                activeDaysMonth = 4,
                activeDaysMonthPercent = 50,
                activeDaysYear = 89,
            ),
            modifier = Modifier
                .width(400.dp)
                .padding(16.dp),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
)
@Composable
private fun Preview2() {
    TraktTheme {
        HomeStreaksContent(
            data = null,
            modifier = Modifier
                .width(400.dp)
                .padding(16.dp),
        )
    }
}
