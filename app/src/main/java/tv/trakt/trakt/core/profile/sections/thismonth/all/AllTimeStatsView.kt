package tv.trakt.trakt.core.profile.sections.thismonth.all

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberNumberFormat
import tv.trakt.trakt.common.model.UserStats
import tv.trakt.trakt.core.profile.sections.thismonth.model.ProfileStats
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Month

private val TileShape = RoundedCornerShape(12.dp)

private data class StatTile(
    @param:StringRes val labelRes: Int,
    val value: Int?,
)

private data class StatSection(
    @param:StringRes val titleRes: Int,
    val tiles: List<StatTile>,
)

@Composable
internal fun AllTimeStatsView(
    stats: ProfileStats,
    modifier: Modifier = Modifier,
    isVip: Boolean = false,
    onYearClick: (year: Int) -> Unit = {},
) {
    val sections = remember(stats) { stats.toSections() }
    val currentYear = remember {
        val now = nowLocal()
        if (now.month == Month.JANUARY) now.year - 1 else now.year
    }

    Column(
        verticalArrangement = spacedBy(20.dp),
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        TraktHeader(
            title = stringResource(R.string.page_title_all_time_stats),
        )

        sections.forEach { section ->
            Column(verticalArrangement = spacedBy(10.dp)) {
                Text(
                    text = stringResource(section.titleRes),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                )

                section.tiles.chunked(3).forEach { row ->
                    Row(
                        horizontalArrangement = spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        row.forEach { tile ->
                            StatTileView(
                                tile = tile,
                                locked = !isVip,
                                modifier = Modifier.weight(1F),
                            )
                        }
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1F))
                        }
                    }
                }
            }
        }

        if (!isVip) {
            Text(
                text = stringResource(R.string.text_vip_upsell_more_stats),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmall,
            )
        }

        Column(verticalArrangement = spacedBy(12.dp)) {
            ReviewLink(
                text = stringResource(R.string.button_text_year_in_review, (currentYear - 1).toString()),
                iconRes = R.drawable.ic_history,
                onClick = { onYearClick(currentYear - 1) },
            )
            ReviewLink(
                text = stringResource(R.string.button_text_year_to_date),
                iconRes = R.drawable.ic_calendar,
                onClick = { onYearClick(currentYear) },
            )
        }
    }
}

@Composable
private fun StatTileView(
    tile: StatTile,
    modifier: Modifier = Modifier,
    locked: Boolean = true,
) {
    Column(
        verticalArrangement = spacedBy(4.dp),
        modifier = modifier
            .shadow(TraktTheme.colors.shadowDynamicDefault, TileShape)
            .background(TraktTheme.colors.dialogOnContainer, TileShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = stringResource(tile.labelRes),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.meta,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (tile.value == null && locked) {
            Icon(
                painter = painterResource(R.drawable.ic_vip_crown),
                contentDescription = null,
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .size(18.dp),
            )
        } else {
            Text(
                text = tile.value?.let { rememberNumberFormat(it) } ?: "-",
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(24.dp),
            )
        }
    }
}

@Composable
private fun ReviewLink(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
        modifier = Modifier.onClick(onClick = onClick),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.buttonSecondary,
        )
        Icon(
            painter = painterResource(R.drawable.ic_external),
            contentDescription = null,
            tint = TraktTheme.colors.textSecondary,
            modifier = Modifier.size(14.dp),
        )
    }
}

private fun ProfileStats.toSections(): List<StatSection> {
    val vip: UserStats? = allTime

    return listOf(
        StatSection(
            titleRes = R.string.header_stats_watched,
            tiles = listOf(
                StatTile(R.string.label_stats_plays, vip?.totalPlays),
                StatTile(R.string.label_stats_hours, vip?.let { Math.round(it.totalMinutes / 60.0).toInt() }),
                StatTile(R.string.label_stats_movies, vip?.moviesWatched ?: allMoviesCount),
                StatTile(R.string.label_stats_shows, vip?.showsWatched ?: allShowsCount),
                StatTile(R.string.label_stats_episodes, vip?.episodesWatched ?: allEpisodesCount),
            ),
        ),
        StatSection(
            titleRes = R.string.header_stats_contributions,
            tiles = listOf(
                StatTile(R.string.label_stats_ratings, vip?.ratings),
                StatTile(R.string.label_stats_comments, vip?.comments),
                StatTile(R.string.stat_text_lists, vip?.lists),
            ),
        ),
        StatSection(
            titleRes = R.string.header_stats_progress,
            tiles = listOf(
                StatTile(R.string.tag_text_started, vip?.progressStarted),
                StatTile(R.string.label_stats_finished, vip?.progressFinished),
                StatTile(R.string.tag_text_dropped, vip?.progressDropped),
            ),
        ),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF131517)
@Composable
private fun Preview() {
    TraktTheme {
        AllTimeStatsView(
            isVip = true,
            stats = ProfileStats(
                allMoviesCount = 145,
                allShowsCount = 87,
                allEpisodesCount = 2310,
                allTime = UserStats(
                    totalMinutes = 123_456,
                    totalPlays = 4_217,
                    moviesWatched = 145,
                    showsWatched = 87,
                    episodesWatched = 2310,
                    ratings = 512,
                    comments = 34,
                    lists = 12,
                    progressStarted = 40,
                    progressFinished = 30,
                    progressDropped = 5,
                ),
            ),
        )
    }
}
