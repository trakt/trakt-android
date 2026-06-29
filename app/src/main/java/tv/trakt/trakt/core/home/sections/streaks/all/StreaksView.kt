package tv.trakt.trakt.core.home.sections.streaks.all

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.yearMonthFormat
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.core.home.sections.streaks.all.ui.StreaksMonthGrid
import tv.trakt.trakt.core.home.sections.streaks.model.MonthlyStreakData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.YearMonth

@Composable
internal fun StreaksView(
    viewModel: StreaksViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    state.data?.let { data ->
        state.mode?.let { mode ->
            StreaksViewContent(
                data = data,
                mode = mode,
                modifier = modifier,
            )
        }
    }

    if (state.data == null) {
        FilmProgressIndicator(
            modifier = Modifier
                .padding(vertical = 112.dp)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun StreaksViewContent(
    mode: MediaMode,
    data: MonthlyStreakData,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .verticalScroll(
                state = rememberScrollState(),
            ),
    ) {
        TraktHeader(
            title = stringResource(R.string.drawer_title_streak_activity),
            subtitle = "${
                YearMonth.now().format(yearMonthFormat()).capitalize()
            }  •  ${stringResource(mode.displayRes)}",
            modifier = Modifier.padding(bottom = 12.dp),
        )

        StreaksMonthGrid(
            data = data.activity,
            showHeader = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = 12.dp,
                ),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            StreakStatCard(
                value = data.currentStreakTotal.toString(),
                label = stringResource(R.string.label_stats_current_streak),
                subtitle = stringResource(R.string.text_stats_keep_it_going),
                modifier = Modifier.weight(1f),
            )
            StreakStatCard(
                value = data.previousStreakTotal.toString(),
                label = stringResource(R.string.label_stats_previous_streak),
                subtitle = stringResource(R.string.label_stats_days_active)
                    .lowercase()
                    .capitalize(),
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            StreakStatCard(
                value = data.currentStreak.toString(),
                label = stringResource(R.string.label_stats_current_monthly_streak),
                subtitle = stringResource(R.string.text_this_month),
                modifier = Modifier.weight(1f),
            )
            StreakStatCard(
                value = data.droppedStreaks.toString(),
                label = stringResource(R.string.label_stats_dropped_streaks),
                subtitle = stringResource(R.string.text_this_month),
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            StreakStatCard(
                value = "${data.activeDaysMonth} (${data.activeDaysMonthPercent}%)",
                label = stringResource(R.string.label_stats_active_days),
                subtitle = stringResource(R.string.text_this_month),
                modifier = Modifier.weight(1f),
            )
            StreakStatCard(
                value = data.activeDaysYear.toString(),
                label = stringResource(R.string.label_stats_active_days),
                subtitle = stringResource(R.string.text_this_year),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StreakStatCard(
    value: String,
    label: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .background(
                color = TraktTheme.colors.dialogOnContainer,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = value,
            style = TraktTheme.typography.heading3.copy(
                fontSize = 22.sp,
                letterSpacing = 0.02.sp,
            ),
            color = Purple400,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 2.dp),
        )
        Text(
            text = label,
            style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = FontWeight.W600),
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = subtitle,
            style = TraktTheme.typography.paragraphSmaller,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewLoading() {
    TraktTheme {
        FilmProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        StreaksViewContent(
            mode = MediaMode.Movies,
            data = MonthlyStreakData(
                activity = persistentMapOf(),
                currentStreakTotal = 128,
                previousStreakTotal = 45,
                currentStreak = 1,
                previousStreak = 2,
                droppedStreaks = 2,
                activeDaysMonth = 4,
                activeDaysMonthPercent = 50,
                activeDaysYear = 89,
            ),
        )
    }
}
