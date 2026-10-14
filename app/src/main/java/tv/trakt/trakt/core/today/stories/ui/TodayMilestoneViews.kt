package tv.trakt.trakt.core.today.stories.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.theme.colors.LightColors
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.core.today.model.TodayMilestone
import tv.trakt.trakt.core.today.model.TodayMilestone.Type
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val ChipShape = RoundedCornerShape(50)
private val ChipIconSize = 13.dp

@Composable
internal fun TodayMilestoneChip(
    milestone: TodayMilestone,
    modifier: Modifier = Modifier,
) {
    val isFinale = milestone.type == Type.SeasonEnd || milestone.type == Type.SeriesEnd

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = modifier
            .background(Purple500, ChipShape)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Icon(
            painter = painterResource(
                if (isFinale) R.drawable.ic_flag_checker else R.drawable.ic_start,
            ),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(ChipIconSize),
        )
        Text(
            text = milestoneText(milestone),
            style = TraktTheme.typography.meta,
            color = Color.White,
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
