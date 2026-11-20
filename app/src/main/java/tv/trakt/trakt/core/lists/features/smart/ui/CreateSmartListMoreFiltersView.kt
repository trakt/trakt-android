package tv.trakt.trakt.core.lists.features.smart.ui

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.globalfilter.EXCLUSION_PREFIX
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Region
import tv.trakt.trakt.common.model.lists.AwardCeremony
import tv.trakt.trakt.common.model.lists.SMART_LIST_OPEN_VOTES_CEILING
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListLanguage
import tv.trakt.trakt.common.model.lists.SmartListPeopleRole
import tv.trakt.trakt.core.filters.RowsSpacing
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownMultiView
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownOption
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownView
import tv.trakt.trakt.core.filters.views.dropdowns.toFilterValues
import tv.trakt.trakt.core.lists.features.smart.search.SmartListSearchKind
import tv.trakt.trakt.core.lists.features.smart.search.SmartListSearchSheet
import tv.trakt.trakt.core.lists.features.smart.search.toSearchValueLabel
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.CountBadge
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.switch.TraktSwitch
import tv.trakt.trakt.ui.theme.TraktTheme
import java.text.NumberFormat
import java.util.Locale

private val ReleaseWindowDays = listOf(30, 90, 180, 365)
private val VoteSteps = listOf(100, 1_000, 10_000, 100_000)

private enum class MoreRating(
    val label: String,
    val max: Float,
) {
    Tmdb("TMDB", 10f),
    Letterboxd("Letterboxd", 5f),
    Mal("MAL", 10f),
}

private const val RATING_STEP = 0.5f
private const val AWARD_YEARS_MIN = 1930

@Composable
internal fun CreateSmartListMoreFiltersView(
    filters: SmartListFilters,
    awardCatalog: ImmutableList<AwardCeremony>,
    languages: ImmutableList<SmartListLanguage>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onFiltersChange: (SmartListFilters) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180F else 0F,
        label = "chevronRotation",
    )

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .onClick { onExpandedChange(!expanded) },
        ) {
            Row(
                horizontalArrangement = spacedBy(6.dp),
                verticalAlignment = CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.header_smart_list_more_filters),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.buttonTertiary,
                )

                if (filters.moreFiltersCount > 0) {
                    CountBadge(count = filters.moreFiltersCount)
                }
            }

            Icon(
                painter = painterResource(R.drawable.ic_arrow_dropdown),
                contentDescription = null,
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer {
                        rotationZ = chevronRotation
                    },
            )
        }

        if (expanded) {
            Column(
                verticalArrangement = spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
            ) {
                SearchFilters(
                    filters = filters,
                    onFiltersChange = onFiltersChange,
                    enabled = enabled,
                )

                LanguageFilters(
                    filters = filters,
                    languages = languages,
                    onFiltersChange = onFiltersChange,
                    enabled = enabled,
                )

                AwardsFilters(
                    filters = filters,
                    awardCatalog = awardCatalog,
                    onFiltersChange = onFiltersChange,
                    enabled = enabled,
                )

                ReleaseAndVotesFilters(
                    filters = filters,
                    onFiltersChange = onFiltersChange,
                    enabled = enabled,
                )

                MoreRatingsFilters(
                    filters = filters,
                    onFiltersChange = onFiltersChange,
                    enabled = enabled,
                )

                HideFilters(
                    filters = filters,
                    onFiltersChange = onFiltersChange,
                    enabled = enabled,
                )

                FilterSection(
                    title = stringResource(R.string.option_text_certification_parental_guidance),
                    spacing = 2.dp,
                ) {
                    SwitchRow(
                        label = stringResource(R.string.toggle_text_include_unrated_parental),
                        checked = filters.parentalIncludeUnrated,
                        enabled = enabled,
                        onCheckedChange = { onFiltersChange(filters.copy(parentalIncludeUnrated = it)) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchFilters(
    filters: SmartListFilters,
    onFiltersChange: (SmartListFilters) -> Unit,
    enabled: Boolean,
) {
    var searchKind by remember { mutableStateOf<SmartListSearchKind?>(null) }
    val kinds = SmartListSearchKind.entries.filter {
        it != SmartListSearchKind.Networks || filters.media != MediaMode.Movies
    }

    Column(
        verticalArrangement = spacedBy(RowsSpacing),
        modifier = Modifier.fillMaxWidth(),
    ) {
        kinds.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = spacedBy(RowsSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { kind ->
                    SearchFilterField(
                        header = stringResource(kind.headerRes),
                        values = filters.searchValues(kind),
                        enabled = enabled,
                        onClick = { searchKind = kind },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        SearchSettings(
            filters = filters,
            onFiltersChange = onFiltersChange,
            enabled = enabled,
        )
    }

    SmartListSearchSheet(
        kind = searchKind,
        values = searchKind?.let { filters.searchValues(it) } ?: persistentListOf(),
        onValuesChange = { values ->
            searchKind?.let { onFiltersChange(filters.withSearchValues(it, values)) }
        },
        onDismiss = { searchKind = null },
    )
}

@Composable
private fun SearchSettings(
    filters: SmartListFilters,
    onFiltersChange: (SmartListFilters) -> Unit,
    enabled: Boolean,
) {
    val matchOptions = persistentListOf(
        ChipOption(false, stringResource(R.string.option_text_match_any)),
        ChipOption(true, stringResource(R.string.option_text_match_all)),
    )
    val matchLabel = stringResource(R.string.label_smart_list_match)
    val keywordsHeader = stringResource(R.string.header_smart_list_keywords)
    val peopleHeader = stringResource(R.string.header_smart_list_people)

    if (filters.keywords.includedCount() > 1) {
        ChipSelectFilter(
            title = stringResource(R.string.text_smart_list_recipe_detail, keywordsHeader, matchLabel),
            options = matchOptions,
            selected = filters.keywordsMatchAll,
            enabled = enabled,
            onSelect = { onFiltersChange(filters.copy(keywordsMatchAll = it)) },
            modifier = Modifier.padding(top = 8.dp),
        )
    }

    if (filters.people.includedCount() > 1) {
        ChipSelectFilter(
            title = stringResource(R.string.text_smart_list_recipe_detail, peopleHeader, matchLabel),
            options = matchOptions,
            selected = filters.peopleMatchAll,
            enabled = enabled,
            onSelect = { onFiltersChange(filters.copy(peopleMatchAll = it)) },
            modifier = Modifier.padding(top = 8.dp),
        )
    }

    if (!filters.people.isNullOrEmpty()) {
        ChipSelectFilter(
            title = stringResource(
                R.string.text_smart_list_recipe_detail,
                peopleHeader,
                stringResource(R.string.label_smart_list_people_role),
            ),
            options = persistentListOf<ChipOption<SmartListPeopleRole?>>(
                ChipOption(null, stringResource(R.string.option_text_role_any)),
            ).addAll(
                SmartListPeopleRole.entries.map { ChipOption(it, stringResource(it.displayRes)) },
            ),
            selected = filters.peopleRole,
            enabled = enabled,
            onSelect = { onFiltersChange(filters.copy(peopleRole = it)) },
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun SearchFilterField(
    header: String,
    values: ImmutableList<String>,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = values.isNotEmpty()
    val labels = values.map { token ->
        when {
            token.startsWith(EXCLUSION_PREFIX) -> stringResource(
                R.string.option_text_excluded,
                token.removePrefix(EXCLUSION_PREFIX).toSearchValueLabel(),
            )
            else -> token.toSearchValueLabel()
        }
    }

    Text(
        text = labels.joinToString(", ").ifEmpty { header },
        style = TraktTheme.typography.buttonTertiary,
        color = when (active) {
            true -> TraktTheme.colors.dropdownContentActive
            false -> TraktTheme.colors.dropdownContent
        },
        maxLines = 1,
        overflow = Ellipsis,
        modifier = modifier
            .background(
                color = when (active) {
                    true -> TraktTheme.colors.dropdownContainerActive
                    false -> TraktTheme.colors.dropdownContainer
                },
                shape = RoundedCornerShape(15.dp),
            )
            .onClick(enabled = enabled, onClick = onClick)
            .padding(13.dp),
    )
}

@Composable
private fun LanguageFilters(
    filters: SmartListFilters,
    languages: ImmutableList<SmartListLanguage>,
    onFiltersChange: (SmartListFilters) -> Unit,
    enabled: Boolean,
) {
    val allOption = DropdownOption<String?>(null, stringResource(R.string.option_text_all))
    val languageOptions = remember(languages, allOption) {
        (listOf(allOption) + languages.map { DropdownOption<String?>(it.code, it.name) })
            .toImmutableList()
    }
    val languageValues = remember(filters.languages, languages, allOption) {
        when (val selected = filters.languages) {
            null -> persistentListOf(allOption)
            else -> selected.map { code ->
                DropdownOption<String?>(code, languages.firstOrNull { it.code == code }?.name ?: code)
            }.toImmutableList()
        }
    }

    val countryAny = DropdownOption<String?>(null, stringResource(R.string.option_text_any_country))
    val countryOptions = remember(countryAny) {
        (
            listOf(countryAny) +
                Region.AllLocales.map { DropdownOption<String?>(it.country.lowercase(), it.displayCountry) }
        )
            .toImmutableList()
    }
    val countryValue = filters.watchnowCountry
        ?.let { code -> DropdownOption<String?>(code, Locale("", code).displayCountry) }
        ?: countryAny

    Row(
        horizontalArrangement = spacedBy(RowsSpacing),
        modifier = Modifier.fillMaxWidth(),
    ) {
        DropdownMultiView(
            header = stringResource(R.string.header_smart_list_languages),
            active = !filters.languages.isNullOrEmpty(),
            values = languageValues,
            options = languageOptions,
            onOptionsSelected = { options ->
                onFiltersChange(filters.copy(languages = options.toFilterValues()))
            },
            enabled = enabled,
            modifier = Modifier.weight(1f),
        )

        DropdownView(
            header = stringResource(R.string.header_smart_list_streaming_country),
            value = countryValue,
            active = filters.watchnowCountry != null,
            options = countryOptions,
            onOptionSelected = { option ->
                onFiltersChange(filters.copy(watchnowCountry = option.raw))
            },
            enabled = enabled,
            modifier = Modifier.weight(1f),
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

    FilterSection(title = stringResource(R.string.header_smart_list_awards)) {
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

@Composable
private fun ReleaseAndVotesFilters(
    filters: SmartListFilters,
    onFiltersChange: (SmartListFilters) -> Unit,
    enabled: Boolean,
) {
    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        ChipSelectFilter(
            title = stringResource(R.string.header_smart_list_released_within),
            options = persistentListOf<ChipOption<Int?>>(
                ChipOption(null, stringResource(R.string.option_text_any_time)),
            ).addAll(
                ReleaseWindowDays.map {
                    ChipOption(it, stringResource(R.string.option_text_last_days, it.toString()))
                },
            ),
            selected = filters.releasedWithinDays,
            enabled = enabled,
            onSelect = { onFiltersChange(filters.copy(releasedWithinDays = it)) },
        )

        VotesFilter(
            source = "Trakt",
            votes = filters.votes,
            enabled = enabled,
            onSelect = { onFiltersChange(filters.copy(votes = it)) },
        )

        VotesFilter(
            source = "IMDb",
            votes = filters.imdbVotes,
            enabled = enabled,
            onSelect = { onFiltersChange(filters.copy(imdbVotes = it)) },
        )
    }
}

@Composable
private fun VotesFilter(
    source: String,
    votes: ImmutableList<Int>?,
    enabled: Boolean,
    onSelect: (ImmutableList<Int>?) -> Unit,
) {
    val options = persistentListOf<ChipOption<Int?>>(
        ChipOption(null, stringResource(R.string.option_text_any_votes)),
    ).addAll(
        VoteSteps.map {
            ChipOption(it, stringResource(R.string.option_text_votes_from, rememberThousandsFormat(it)))
        },
    )

    ChipSelectFilter(
        title = stringResource(R.string.label_smart_list_min_votes, source),
        options = options,
        selected = votes?.firstOrNull(),
        enabled = enabled,
        onSelect = { min ->
            onSelect(min?.let { persistentListOf(it, SMART_LIST_OPEN_VOTES_CEILING) })
        },
    )
}

@Composable
private fun MoreRatingsFilters(
    filters: SmartListFilters,
    onFiltersChange: (SmartListFilters) -> Unit,
    enabled: Boolean,
) {
    FilterSection(title = stringResource(R.string.header_smart_list_more_ratings)) {
        MoreRating.entries.forEach { rating ->
            RatingRangeFilter(
                rating = rating,
                range = filters.moreRating(rating),
                enabled = enabled,
                onRangeChange = { onFiltersChange(filters.withMoreRating(rating, it)) },
            )
        }
    }
}

@Composable
private fun RatingRangeFilter(
    rating: MoreRating,
    range: ImmutableList<Float>?,
    enabled: Boolean,
    onRangeChange: (ImmutableList<Float>?) -> Unit,
) {
    val fullRange = 0f..rating.max
    val rangeValue = remember(range) {
        mutableStateOf(
            (range?.getOrNull(0) ?: 0f)..(range?.getOrNull(1) ?: rating.max),
        )
    }

    val configuration = LocalConfiguration.current
    val numberFormat = remember(configuration) {
        val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 1 }
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(
                R.string.list_summary_range_labeled_between,
                rating.label,
                numberFormat.format(rangeValue.value.start),
                numberFormat.format(rangeValue.value.endInclusive),
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
            steps = (rating.max / RATING_STEP).toInt() - 1,
            onValueChange = { rangeValue.value = it },
            onValueChangeFinished = {
                val value = rangeValue.value
                onRangeChange(
                    when (value == fullRange) {
                        true -> null
                        false -> persistentListOf(value.start, value.endInclusive)
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

@Composable
private fun traktSliderColors(): SliderColors {
    return SliderDefaults.colors(
        activeTrackColor = TraktTheme.colors.accent,
        inactiveTrackColor = Color.White,
        thumbColor = TraktTheme.colors.accent,
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent,
        disabledActiveTrackColor = TraktTheme.colors.dialogOnContainer,
        disabledActiveTickColor = TraktTheme.colors.dialogOnContainer,
        disabledThumbColor = TraktTheme.colors.dialogOnContainer,
        disabledInactiveTrackColor = TraktTheme.colors.dialogOnContainer,
        disabledInactiveTickColor = TraktTheme.colors.dialogOnContainer,
    )
}

@Composable
private fun HideFilters(
    filters: SmartListFilters,
    onFiltersChange: (SmartListFilters) -> Unit,
    enabled: Boolean,
) {
    val hasShows = filters.media != MediaMode.Movies

    FilterSection(
        title = stringResource(R.string.header_smart_list_hide),
        spacing = 2.dp,
    ) {
        if (hasShows) {
            SwitchRow(
                label = stringResource(R.string.toggle_text_hide_watching),
                checked = filters.ignoreWatching,
                enabled = enabled,
                onCheckedChange = { onFiltersChange(filters.copy(ignoreWatching = it)) },
            )
        }
        SwitchRow(
            label = stringResource(R.string.toggle_text_hide_unreleased),
            checked = filters.ignoreUnreleased,
            enabled = enabled,
            onCheckedChange = { onFiltersChange(filters.copy(ignoreUnreleased = it)) },
        )
        SwitchRow(
            label = stringResource(R.string.toggle_text_hide_released),
            checked = filters.ignoreReleased,
            enabled = enabled,
            onCheckedChange = { onFiltersChange(filters.copy(ignoreReleased = it)) },
        )
        if (hasShows) {
            SwitchRow(
                label = stringResource(R.string.toggle_text_hide_ended),
                checked = filters.ignoreEnded,
                enabled = enabled,
                onCheckedChange = { onFiltersChange(filters.copy(ignoreEnded = it)) },
            )
            SwitchRow(
                label = stringResource(R.string.toggle_text_hide_airing),
                checked = filters.ignoreAiring,
                enabled = enabled,
                onCheckedChange = { onFiltersChange(filters.copy(ignoreAiring = it)) },
            )
        }
        SwitchRow(
            label = stringResource(R.string.toggle_text_hide_no_release_date),
            checked = filters.ignoreNoReleaseDate,
            enabled = enabled,
            onCheckedChange = { onFiltersChange(filters.copy(ignoreNoReleaseDate = it)) },
        )
    }
}

@Composable
private fun FilterSection(
    title: String,
    spacing: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(spacing),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.buttonTertiary,
        )
        content()
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = spacedBy(12.dp),
        verticalAlignment = CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        TraktSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
        Text(
            text = label,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.buttonTertiary,
            maxLines = 2,
            overflow = Ellipsis,
            modifier = Modifier.clickable(enabled = enabled) {
                onCheckedChange(!checked)
            },
        )
    }
}

internal data class ChipOption<T>(
    val value: T,
    val label: String,
)

@Composable
internal fun <T> ChipSelectFilter(
    title: String,
    options: ImmutableList<ChipOption<T>>,
    selected: T,
    enabled: Boolean,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.buttonTertiary,
            maxLines = 1,
            overflow = Ellipsis,
        )

        FlowRow(
            horizontalArrangement = spacedBy(6.dp),
            verticalArrangement = spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option.value == selected,
                    text = option.label,
                    height = 32.dp,
                    onClick = {
                        if (enabled && option.value != selected) {
                            onSelect(option.value)
                        }
                    },
                )
            }
        }
    }
}

private fun SmartListFilters.searchValues(kind: SmartListSearchKind): ImmutableList<String> {
    val values = when (kind) {
        SmartListSearchKind.Keywords -> keywords
        SmartListSearchKind.People -> people
        SmartListSearchKind.Studios -> studios
        SmartListSearchKind.Networks -> networks
    }
    return values ?: persistentListOf()
}

private fun SmartListFilters.withSearchValues(
    kind: SmartListSearchKind,
    values: ImmutableList<String>,
): SmartListFilters {
    val stored = values.takeIf { it.isNotEmpty() }
    return when (kind) {
        SmartListSearchKind.Keywords -> copy(keywords = stored)
        SmartListSearchKind.People -> copy(people = stored, peopleRole = peopleRole.takeIf { stored != null })
        SmartListSearchKind.Studios -> copy(studios = stored)
        SmartListSearchKind.Networks -> copy(networks = stored)
    }
}

private fun List<String>?.includedCount(): Int = orEmpty().count { !it.startsWith(EXCLUSION_PREFIX) }

private fun SmartListFilters.moreRating(rating: MoreRating): ImmutableList<Float>? {
    return when (rating) {
        MoreRating.Tmdb -> tmdbRatings
        MoreRating.Letterboxd -> letterboxdRatings
        MoreRating.Mal -> malRatings
    }
}

private fun SmartListFilters.withMoreRating(
    rating: MoreRating,
    range: ImmutableList<Float>?,
): SmartListFilters {
    return when (rating) {
        MoreRating.Tmdb -> copy(tmdbRatings = range)
        MoreRating.Letterboxd -> copy(letterboxdRatings = range)
        MoreRating.Mal -> copy(malRatings = range)
    }
}

@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF151418,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = 2000,
)
// @Preview(
//    name = "Light",
//    showBackground = true,
//    backgroundColor = 0xFFF1EFF4,
//    uiMode = Configuration.UI_MODE_NIGHT_NO,
//    heightDp = 2000,
// )
@Composable
private fun Preview() {
    TraktThemePreview {
        CreateSmartListMoreFiltersView(
            filters = SmartListFilters.Default.copy(
                media = MediaMode.Shows,
                releasedWithinDays = 90,
                votes = persistentListOf(1_000, SMART_LIST_OPEN_VOTES_CEILING),
                tmdbRatings = persistentListOf(6.5f, 10f),
                ignoreEnded = true,
                languages = persistentListOf("en"),
                keywords = persistentListOf("time travel", "${EXCLUSION_PREFIX}dystopia"),
                people = persistentListOf("christopher-nolan"),
                peopleRole = SmartListPeopleRole.Directing,
                watchnowCountry = "us",
                awards = persistentListOf("best-picture"),
                awardWon = true,
            ),
            awardCatalog = persistentListOf(
                AwardCeremony(
                    slug = "academy-awards",
                    name = "Academy Awards",
                    forMovies = true,
                    forShows = false,
                    awards = persistentListOf(AwardCeremony.Award("best-picture", "Best Picture")),
                ),
            ),
            languages = persistentListOf(SmartListLanguage("en", "English")),
            expanded = true,
            onExpandedChange = {},
            onFiltersChange = {},
        )
    }
}

// @PreviewTraktLightDark
// @Composable
// private fun PreviewCollapsed() {
//    TraktThemePreview {
//        CreateSmartListMoreFiltersView(
//            filters = SmartListFilters.Default.copy(
//                media = MediaMode.Movies,
//                ignoreUnreleased = true,
//            ),
//            awardCatalog = persistentListOf(),
//            languages = persistentListOf(),
//            expanded = false,
//            onExpandedChange = {},
//            onFiltersChange = {},
//        )
//    }
// }
