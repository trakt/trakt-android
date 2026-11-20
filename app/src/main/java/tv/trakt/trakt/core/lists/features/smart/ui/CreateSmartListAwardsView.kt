package tv.trakt.trakt.core.lists.features.smart.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.lists.AwardCeremony
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownMultiView
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownOption
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val AWARD_YEARS_MIN = 1930

@Composable
internal fun CreateSmartListAwardsView(
    filters: SmartListFilters,
    awardCatalog: ImmutableList<AwardCeremony>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onFiltersChange: (SmartListFilters) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    CollapsibleFilterSection(
        title = stringResource(R.string.header_smart_list_awards),
        count = filters.awardFiltersCount,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
    ) {
        AwardsFilters(
            filters = filters,
            awardCatalog = awardCatalog,
            onFiltersChange = onFiltersChange,
            enabled = enabled,
        )
    }
}

@Composable
private fun AwardsFilters(
    filters: SmartListFilters,
    awardCatalog: ImmutableList<AwardCeremony>,
    onFiltersChange: (SmartListFilters) -> Unit,
    enabled: Boolean,
) {
    val hasAwards = !filters.awards.isNullOrEmpty()

    val allOption = DropdownOption<String?>(null, stringResource(R.string.option_text_all))
    val awardOptions = remember(awardCatalog, filters.media, allOption) {
        val awards = awardCatalog
            .filter { it.supports(filters.media) }
            .flatMap { ceremony ->
                ceremony.awards.map { DropdownOption<String?>(it.slug, it.name, ceremony.name) }
            }
        (listOf(allOption) + awards).toImmutableList()
    }
    val awardValues = remember(filters.awards, awardOptions, allOption) {
        when (val selected = filters.awards) {
            null -> persistentListOf(allOption)
            else -> selected.map { slug ->
                awardOptions.firstOrNull { it.raw == slug } ?: DropdownOption<String?>(slug, slug)
            }.toImmutableList()
        }
    }

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
    ) {
        DropdownMultiView(
            header = stringResource(R.string.header_smart_list_award_categories),
            active = hasAwards,
            values = awardValues,
            options = awardOptions,
            onOptionsSelected = { options ->
                onFiltersChange(filters.withAwards(options.mapNotNull { it.raw }))
            },
            enabled = enabled,
        )

        SwitchRow(
            label = stringResource(R.string.toggle_text_award_winners_only),
            checked = filters.awardWon,
            enabled = enabled && hasAwards,
            onCheckedChange = { onFiltersChange(filters.copy(awardWon = it)) },
        )

        AwardYearsFilter(
            years = filters.awardYears,
            enabled = enabled && hasAwards,
            onYearsChange = { onFiltersChange(filters.copy(awardYears = it)) },
        )
    }
}

@Composable
private fun AwardYearsFilter(
    years: ImmutableList<Int>?,
    enabled: Boolean,
    onYearsChange: (ImmutableList<Int>?) -> Unit,
) {
    val maxYear = remember { nowLocalDay().year }
    val fullRange = AWARD_YEARS_MIN.toFloat()..maxYear.toFloat()
    val rangeValue = remember(years, maxYear) {
        mutableStateOf(
            (years?.getOrNull(0) ?: AWARD_YEARS_MIN).toFloat()..(years?.getOrNull(1) ?: maxYear).toFloat(),
        )
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(
                R.string.text_smart_list_recipe_detail,
                stringResource(R.string.label_smart_list_ceremony_year),
                stringResource(
                    R.string.list_summary_range_between,
                    rangeValue.value.start.toInt().toString(),
                    rangeValue.value.endInclusive.toInt().toString(),
                ),
            ),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.buttonTertiary,
            maxLines = 1,
            overflow = Ellipsis,
        )

        RangeSlider(
            value = rangeValue.value,
            enabled = enabled,
            valueRange = fullRange,
            steps = maxYear - AWARD_YEARS_MIN - 1,
            onValueChange = { rangeValue.value = it },
            onValueChangeFinished = {
                val start = rangeValue.value.start.toInt()
                val end = rangeValue.value.endInclusive.toInt()
                onYearsChange(
                    when (start == AWARD_YEARS_MIN && end == maxYear) {
                        true -> null
                        false -> persistentListOf(start, end)
                    },
                )
            },
            colors = traktSliderColors(),
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp),
        )
    }
}

private val PreviewCatalog = persistentListOf(
    AwardCeremony(
        slug = "academy-awards",
        name = "Academy Awards",
        forMovies = true,
        forShows = false,
        awards = persistentListOf(
            AwardCeremony.Award("best-picture", "Best Picture"),
            AwardCeremony.Award("best-director", "Best Director"),
        ),
    ),
)

@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF151418,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = 400,
)
@Preview(
    name = "Light",
    showBackground = true,
    backgroundColor = 0xFFF1EFF4,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    heightDp = 400,
)
@Composable
private fun Preview() {
    TraktThemePreview {
        CreateSmartListAwardsView(
            filters = SmartListFilters.Default.copy(
                media = MediaMode.Movies,
                awards = persistentListOf("best-picture"),
                awardYears = persistentListOf(1990, 2010),
                awardWon = true,
            ),
            awardCatalog = PreviewCatalog,
            expanded = true,
            onExpandedChange = {},
            onFiltersChange = {},
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewCollapsed() {
    TraktThemePreview {
        CreateSmartListAwardsView(
            filters = SmartListFilters.Default,
            awardCatalog = PreviewCatalog,
            expanded = false,
            onExpandedChange = {},
            onFiltersChange = {},
        )
    }
}
