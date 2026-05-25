package tv.trakt.trakt.core.filters.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Availability
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Certification
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Region
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterDecade
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterDecade.CurrentYear
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterRuntime
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterRuntime.Runtime120Plus
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.core.filters.ColumnsSpacing
import tv.trakt.trakt.core.filters.GlobalFiltersState
import tv.trakt.trakt.core.filters.RowsSpacing
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownOption
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownView
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.switch.TraktSwitch
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun GlobalFiltersSimpleView(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit = { _, _ -> },
    onResetFilter: () -> Unit = { },
    onToggleMode: (GlobalFilterMode) -> Unit = { },
) {
    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TraktHeader(
                title = stringResource(R.string.header_filters),
                subtitle = stringResource(GlobalFilterMode.Simple.displayStringRes),
            )

            Row(
                horizontalArrangement = spacedBy(16.dp),
                verticalAlignment = CenterVertically,
            ) {
                if (state.filter.isActive) {
                    Text(
                        text = stringResource(R.string.text_reset_all),
                        color = Red400,
                        style = TraktTheme.typography.buttonTertiary,
                        modifier = Modifier.clickable(onClick = onResetFilter),
                    )
                }

                FilterChip(
                    selected = false,
                    text = stringResource(GlobalFilterMode.Advanced.displayStringRes),
                    height = 32.dp,
                    leadingAlwaysVisible = true,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_filters_advanced),
                            contentDescription = null,
                            tint = TraktTheme.colors.textPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    onClick = {
                        onToggleMode(GlobalFilterMode.Advanced)
                    },
                )
            }
        }

        MediaModeFilters(
            selected = state.filter.mode,
            onClick = {
                onUpdateFilter(state.filter.copy(mode = it), false)
            },
            height = 32.dp,
            modifier = Modifier.padding(
                top = 4.dp,
                bottom = 5.dp,
            ),
        )

        Column(
            verticalArrangement = spacedBy(ColumnsSpacing),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = spacedBy(RowsSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                GenreFilter(
                    state = state,
                    onUpdateFilter = onUpdateFilter,
                    modifier = Modifier.weight(1f),
                )

                AvailabilityFilter(
                    state = state,
                    onUpdateFilter = onUpdateFilter,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                horizontalArrangement = spacedBy(RowsSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                DecadeFilter(
                    state = state,
                    onUpdateFilter = onUpdateFilter,
                    modifier = Modifier.weight(1f),
                )

                RuntimeFilter(
                    state = state,
                    onUpdateFilter = onUpdateFilter,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                horizontalArrangement = spacedBy(RowsSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CertificationFilter(
                    state = state,
                    onUpdateFilter = onUpdateFilter,
                    modifier = Modifier.weight(1f),
                )

                RegionFilter(
                    state = state,
                    onUpdateFilter = onUpdateFilter,
                    modifier = Modifier.weight(1f),
                )
            }

            RatingFilter(
                state = state,
                onUpdateFilter = onUpdateFilter,
            )

            SwitchesFilter(
                state = state,
                onUpdateFilter = onUpdateFilter,
            )
        }
    }
}

@Composable
private fun GenreFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val genresValue: DropdownOption<MediaGenre?> = state.filter.genre?.let {
        val genre = it.first()
        DropdownOption(
            raw = genre,
            displayString = stringResource(genre.displayStringRes),
        )
    } ?: DropdownOption(
        raw = null,
        displayString = stringResource(R.string.text_all),
    )

    val genresOptions = buildList<DropdownOption<MediaGenre?>> {
        add(
            DropdownOption(null, stringResource(R.string.text_all)),
        )
        addAll(
            MediaGenre.entries
                .map {
                    DropdownOption(
                        raw = it,
                        displayString = stringResource(it.displayStringRes),
                    )
                },
        )
    }.toImmutableList()

    DropdownView(
        header = stringResource(R.string.header_genre),
        active = !state.filter.genre.isNullOrEmpty(),
        value = genresValue,
        options = genresOptions,
        onOptionSelected = { option ->
            onUpdateFilter(
                state.filter.copy(
                    genre = option.raw
                        ?.let { listOf(it) }
                        ?.toImmutableList(),
                ),
                true,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun AvailabilityFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val availabilityValue: DropdownOption<Availability?> = state.filter.availability?.let {
        val availability = it.first()
        DropdownOption(
            raw = availability,
            displayString = stringResource(availability.displayStringRes),
        )
    } ?: DropdownOption(
        raw = null,
        displayString = stringResource(R.string.text_all),
    )

    val availabilityOptions = buildList<DropdownOption<Availability?>> {
        add(
            DropdownOption(null, stringResource(R.string.text_all)),
        )
        addAll(
            Availability.entries
                .map {
                    DropdownOption(
                        raw = it,
                        displayString = stringResource(it.displayStringRes),
                    )
                },
        )
    }.toImmutableList()

    DropdownView(
        header = stringResource(R.string.header_availability),
        active = !state.filter.availability.isNullOrEmpty(),
        value = availabilityValue,
        options = availabilityOptions,
        onOptionSelected = { option ->
            onUpdateFilter(
                state.filter.copy(
                    availability = option.raw
                        ?.let { listOf(it) }
                        ?.toImmutableList(),
                ),
                true,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun DecadeFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val decadeValue: DropdownOption<GlobalFilterDecade?> = state.filter.years?.let { years ->
        val decade = GlobalFilterDecade.entries.find { it.years == years }
        decade?.let {
            DropdownOption(
                raw = it,
                displayString = when {
                    it == CurrentYear -> stringResource(R.string.text_this_year)
                    else -> it.years.first.toString()
                },
            )
        }
    } ?: DropdownOption(
        raw = null,
        displayString = stringResource(R.string.text_all),
    )

    val decadeOptions = buildList<DropdownOption<GlobalFilterDecade?>> {
        add(
            DropdownOption(null, stringResource(R.string.text_all)),
        )
        addAll(
            GlobalFilterDecade.entries
                .map {
                    DropdownOption(
                        raw = it,
                        displayString = when {
                            it == CurrentYear -> stringResource(R.string.text_this_year)
                            else -> it.years.first.toString()
                        },
                    )
                },
        )
    }.toImmutableList()

    DropdownView(
        header = stringResource(R.string.header_decade),
        active = state.filter.years != null,
        value = decadeValue,
        options = decadeOptions,
        onOptionSelected = { option ->
            onUpdateFilter(
                state.filter.copy(
                    years = option.raw?.years,
                ),
                true,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun RuntimeFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val runtimeValue: DropdownOption<GlobalFilterRuntime?> = state.filter.runtime?.let { runtime ->
        val span = GlobalFilterRuntime.entries.find { it.runtime == runtime }
        span?.let {
            DropdownOption(
                raw = it,
                displayString = "${it.runtime.first}-${it.runtime.second}",
            )
        }
    } ?: DropdownOption(
        raw = null,
        displayString = stringResource(R.string.text_all),
    )

    val runtimeOptions = buildList<DropdownOption<GlobalFilterRuntime?>> {
        add(
            DropdownOption(null, stringResource(R.string.text_all)),
        )
        addAll(
            GlobalFilterRuntime.entries
                .map {
                    DropdownOption(
                        raw = it,
                        displayString = when {
                            it == Runtime120Plus -> "${it.runtime.first}+"
                            else -> "${it.runtime.first}-${it.runtime.second}"
                        },
                    )
                },
        )
    }.toImmutableList()

    DropdownView(
        header = stringResource(R.string.header_runtime),
        active = state.filter.runtime != null,
        value = runtimeValue,
        options = runtimeOptions,
        onOptionSelected = { option ->
            onUpdateFilter(
                state.filter.copy(
                    runtime = option.raw?.runtime,
                ),
                true,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun CertificationFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val certificationValue: DropdownOption<Certification?> = state.filter.certification?.let {
        val certification = it.first()
        DropdownOption(
            raw = certification,
            displayString = stringResource(certification.displayStringRes),
        )
    } ?: DropdownOption(
        raw = null,
        displayString = stringResource(R.string.text_all),
    )

    val certificationOptions = buildList<DropdownOption<Certification?>> {
        add(
            DropdownOption(null, stringResource(R.string.text_all)),
        )
        addAll(
            Certification.entries
                .map {
                    DropdownOption(
                        raw = it,
                        displayString = stringResource(it.displayStringRes),
                    )
                },
        )
    }.toImmutableList()

    DropdownView(
        header = stringResource(R.string.header_certification),
        active = !state.filter.certification.isNullOrEmpty(),
        value = certificationValue,
        options = certificationOptions,
        onOptionSelected = { option ->
            onUpdateFilter(
                state.filter.copy(
                    certification = option.raw
                        ?.let { listOf(it) }
                        ?.toImmutableList(),
                ),
                true,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun RegionFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val regionValue: DropdownOption<Region?> = state.filter.region?.let {
        DropdownOption(
            raw = it,
            displayString = stringResource(it.displayStringRes),
        )
    } ?: DropdownOption(
        raw = null,
        displayString = stringResource(R.string.text_all),
    )

    val regionOptions = buildList<DropdownOption<Region?>> {
        add(
            DropdownOption(null, stringResource(R.string.text_all)),
        )
        addAll(
            Region.entries
                .map {
                    DropdownOption(
                        raw = it,
                        displayString = stringResource(it.displayStringRes),
                    )
                },
        )
    }.toImmutableList()

    DropdownView(
        header = stringResource(R.string.header_region),
        active = state.filter.region != null,
        value = regionValue,
        options = regionOptions,
        onOptionSelected = { option ->
            onUpdateFilter(
                state.filter.copy(
                    region = option.raw,
                ),
                true,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun RatingFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    ) {
        val rangeRatingValue = remember(state.filter.rating) {
            mutableStateOf(
                state.filter.rating?.let {
                    it.first.toFloat()..it.second.toFloat()
                } ?: (0f..100f),
            )
        }

        Text(
            text = stringResource(
                R.string.text_trakt_rating_of,
                rangeRatingValue.value.start.toInt(),
                rangeRatingValue.value.endInclusive.toInt(),
            ),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.buttonTertiary,
            maxLines = 1,
            overflow = Ellipsis,
        )

        RangeSlider(
            value = rangeRatingValue.value,
            valueRange = 0f..100f,
            steps = 19, // Step every 5%
            onValueChange = { range ->
                rangeRatingValue.value = range
            },
            onValueChangeFinished = {
                val value = rangeRatingValue.value
                onUpdateFilter(
                    state.filter.copy(
                        rating = value.start.toInt() to value.endInclusive.toInt(),
                    ),
                    true,
                )
            },
            colors = SliderDefaults.colors(
                activeTrackColor = TraktTheme.colors.accent,
                inactiveTrackColor = Color.White,
                thumbColor = TraktTheme.colors.accent,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp),
        )

        Box(
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                for (i in 0..100 step 100) {
                    Text(
                        text = "$i%",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 46.dp),
            ) {
                for (i in 20..80 step 20) {
                    Text(
                        text = "$i%",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchesFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = spacedBy(RowsSpacing),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            val onCheckedChange: (Boolean) -> Unit = remember(state.filter) {
                {
                    onUpdateFilter(
                        state.filter.copy(
                            hideWatched = it,
                        ),
                        false,
                    )
                }
            }

            TraktSwitch(
                checked = state.filter.hideWatched,
                onCheckedChange = onCheckedChange,
            )
            Text(
                text = stringResource(R.string.text_hide_watched),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonTertiary,
                maxLines = 2,
                overflow = Ellipsis,
                modifier = Modifier.clickable {
                    onCheckedChange(!state.filter.hideWatched)
                },
            )
        }

        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            val onCheckedChange: (Boolean) -> Unit = remember(state.filter) {
                {
                    onUpdateFilter(
                        state.filter.copy(
                            hideWatchlist = it,
                        ),
                        false,
                    )
                }
            }

            TraktSwitch(
                checked = state.filter.hideWatchlist,
                onCheckedChange = onCheckedChange,
            )
            Text(
                text = stringResource(R.string.text_hide_watchlist),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonTertiary,
                maxLines = 2,
                overflow = Ellipsis,
                modifier = Modifier.clickable {
                    onCheckedChange(!state.filter.hideWatchlist)
                },
            )
        }
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
        GlobalFiltersSimpleView(state = GlobalFiltersState())
    }
}

@Preview(
    device = "id:pixel_2_xl",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview2() {
    TraktTheme {
        GlobalFiltersSimpleView(
            state = GlobalFiltersState(
                filter = GlobalFilter.Default.copy(
                    genre = persistentListOf(
                        MediaGenre.Action,
                        MediaGenre.Comedy,
                    ),
                    availability = persistentListOf(
                        Availability.AllDigitalReleases,
                    ),
                    years = CurrentYear.years,
                ),
            ),
        )
    }
}
