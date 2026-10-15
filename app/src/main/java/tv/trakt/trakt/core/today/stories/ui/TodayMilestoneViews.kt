package tv.trakt.trakt.core.today.stories.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.HalloweenDarkColors
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.core.today.model.TodayMilestone
import tv.trakt.trakt.core.today.model.TodayMilestone.Type
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val ChipShape = RoundedCornerShape(50)
private val TooltipShape = RoundedCornerShape(8.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodayMilestoneChip(
    milestone: TodayMilestone,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)

    TooltipBox(
        state = tooltipState,
        positionProvider = rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above,
        ),
        tooltip = {
            Text(
                text = milestoneTooltip(milestone),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.tooltipContent,
                modifier = Modifier
                    .background(TraktTheme.colors.tooltipContainer, TooltipShape)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        },
        // Focusable so a tap on the chip while shown only dismisses instead of also reopening.
        focusable = true,
        enableUserInput = false,
        modifier = modifier,
    ) {
        TodayMilestoneChipContent(
            milestone = milestone,
            modifier = Modifier.onClick {
                if (tooltipState.isVisible) {
                    tooltipState.dismiss()
                    return@onClick
                }
                scope.launch {
                    tooltipState.show()
                }
            },
        )
    }
}

@Composable
private fun TodayMilestoneChipContent(
    milestone: TodayMilestone,
    modifier: Modifier = Modifier,
) {
    val isFinale = milestone.type == Type.SeasonEnd || milestone.type == Type.SeriesEnd

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(
            if (isFinale) 3.dp else 6.dp,
        ),
        modifier = modifier
            .background(TraktTheme.colors.accent, ChipShape)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Icon(
            painter = painterResource(
                if (isFinale) R.drawable.ic_flag_checker else R.drawable.ic_clapper,
            ),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimaryOnAccent,
            modifier = Modifier.size(
                if (isFinale) 16.dp else 12.dp,
            ),
        )
        Text(
            text = milestoneText(milestone),
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textPrimaryOnAccent,
            maxLines = 1,
        )
    }
}

@Composable
private fun milestoneText(milestone: TodayMilestone): String {
    return when (milestone.type) {
        Type.SeriesStart,
        Type.SeriesEnd,
        -> stringResource(R.string.translated_value_type_show)

        Type.SeasonStart,
        Type.SeasonEnd,
        -> stringResource(R.string.text_season_number, milestone.season)
    }
}

@Composable
private fun milestoneTooltip(milestone: TodayMilestone): String {
    return when (milestone.type) {
        Type.SeriesStart -> stringResource(R.string.tag_text_today_started_show)
        Type.SeriesEnd -> stringResource(R.string.tag_text_today_finished_show)
        Type.SeasonStart -> stringResource(R.string.tag_text_today_started_season, milestone.season)
        Type.SeasonEnd -> stringResource(R.string.tag_text_today_finished_season, milestone.season)
    }
}

@Composable
private fun PreviewChips(season: Int = 2) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier.padding(16.dp),
    ) {
        Type.entries.forEach {
            TodayMilestoneChip(milestone = TodayMilestone(type = it, season = season))
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        PreviewChips()
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun LightPreview() {
    TraktTheme(colors = LightColors) {
        PreviewChips()
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun HalloweenPreview() {
    TraktTheme(colors = HalloweenDarkColors) {
        PreviewChips()
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun LongSeasonPreview() {
    TraktTheme {
        PreviewChips(season = 128)
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    fontScale = 1.5F,
)
@Composable
private fun LargeFontPreview() {
    TraktTheme {
        PreviewChips()
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "ar-rSA",
)
@Composable
private fun RtlPreview() {
    TraktTheme {
        PreviewChips()
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "de",
)
@Composable
private fun TranslatedPreview() {
    TraktTheme {
        PreviewChips()
    }
}
