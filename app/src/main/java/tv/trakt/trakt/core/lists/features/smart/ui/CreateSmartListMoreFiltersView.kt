package tv.trakt.trakt.core.lists.features.smart.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.lists.SMART_LIST_OPEN_VOTES_CEILING
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
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

@Composable
internal fun CreateSmartListMoreFiltersView(
    filters: SmartListFilters,
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
                    Text(
                        text = filters.moreFiltersCount.toString(),
                        color = TraktTheme.colors.accent,
                        style = TraktTheme.typography.buttonTertiary,
                    )
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

                FilterSection(title = stringResource(R.string.option_text_certification_parental_guidance)) {
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
            colors = SliderDefaults.colors(
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
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp),
        )
    }
}

@Composable
private fun HideFilters(
    filters: SmartListFilters,
    onFiltersChange: (SmartListFilters) -> Unit,
    enabled: Boolean,
) {
    val hasShows = filters.media != MediaMode.Movies

    FilterSection(title = stringResource(R.string.header_smart_list_hide)) {
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
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.meta,
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
        horizontalArrangement = spacedBy(8.dp),
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

@PreviewTraktLightDark
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
            ),
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
        CreateSmartListMoreFiltersView(
            filters = SmartListFilters.Default.copy(
                media = MediaMode.Movies,
                ignoreUnreleased = true,
            ),
            expanded = false,
            onExpandedChange = {},
            onFiltersChange = {},
        )
    }
}
