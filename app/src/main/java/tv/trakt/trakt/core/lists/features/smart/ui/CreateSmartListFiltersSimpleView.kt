package tv.trakt.trakt.core.lists.features.smart.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Availability
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Certification
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Region
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterDecade
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterDecade.CurrentYear
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterRuntime
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterRuntime.Runtime120Plus
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.core.filters.ColumnsSpacing
import tv.trakt.trakt.core.filters.RowsSpacing
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownOption
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownView
import tv.trakt.trakt.core.lists.features.smart.CreateSmartListState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.switch.TraktSwitch
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CreateSmartListFiltersSimpleView(
    state: CreateSmartListState,
    onFiltersChange: (SmartListFilters, Boolean) -> Unit = { _, _ -> },
) {
    val enabled = !state.creating.isLoading

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
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
                    onUpdateFilter = onFiltersChange,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )

                AvailabilityFilter(
                    state = state,
                    onUpdateFilter = onFiltersChange,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                horizontalArrangement = spacedBy(RowsSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                DecadeFilter(
                    state = state,
                    onUpdateFilter = onFiltersChange,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )

                RuntimeFilter(
                    state = state,
                    onUpdateFilter = onFiltersChange,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                horizontalArrangement = spacedBy(RowsSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CertificationFilter(
                    state = state,
                    onUpdateFilter = onFiltersChange,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )

                RegionFilter(
                    state = state,
                    onUpdateFilter = onFiltersChange,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )
            }

            RatingFilter(
                state = state,
                onUpdateFilter = onFiltersChange,
                enabled = enabled,
            )

            SwitchesFilter(
                state = state,
                onUpdateFilter = onFiltersChange,
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun GenreFilter(
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val genresValue: DropdownOption<MediaGenre?> = state.filters.genres?.let {
        val genre = it.first()
        DropdownOption(
            raw = genre,
            displayString = stringResource(genre.displayStringRes),
        )
    } ?: DropdownOption(
        raw = null,
        displayString = stringResource(R.string.option_text_all),
    )

    val genresOptions = buildList<DropdownOption<MediaGenre?>> {
        add(
            DropdownOption(null, stringResource(R.string.option_text_all)),
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
        active = !state.filters.genres.isNullOrEmpty(),
        value = genresValue,
        options = genresOptions,
        onOptionSelected = { option ->
            onUpdateFilter(
                state.filters.copy(
                    genres = option.raw
                        ?.let { listOf(it) }
                        ?.toImmutableList(),
                ),
                true,
            )
        },
        enabled = enabled,
        modifier = modifier,
    )
}

@Composable
private fun AvailabilityFilter(
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val availabilityValue: DropdownOption<Availability?> = state.filters.availability?.let {
        val availability = it.first()
        DropdownOption(
            raw = availability,
            displayString = stringResource(availability.displayStringRes),
        )
    } ?: DropdownOption(
        raw = null,
        displayString = stringResource(R.string.option_text_all),
    )

    val availabilityOptions = buildList<DropdownOption<Availability?>> {
        add(
            DropdownOption(null, stringResource(R.string.option_text_all)),
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
        header = stringResource(R.string.header_streaming),
        active = !state.filters.availability.isNullOrEmpty(),
        value = availabilityValue,
        options = availabilityOptions,
        onOptionSelected = { option ->
            onUpdateFilter(
                state.filters.copy(
                    availability = option.raw
                        ?.let { listOf(it) }
                        ?.toImmutableList(),
                ),
                true,
            )
        },
        enabled = enabled,
        modifier = modifier,
    )
}

@Composable
private fun DecadeFilter(
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val decadeValue: DropdownOption<GlobalFilterDecade?> = state.filters.years?.let { years ->
        val decade = GlobalFilterDecade.entries.find { listOf(it.years.first, it.years.second) == years }
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
        displayString = stringResource(R.string.option_text_all),
    )

    val decadeOptions = buildList<DropdownOption<GlobalFilterDecade?>> {
        add(
            DropdownOption(null, stringResource(R.string.option_text_all)),
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
        active = !state.filters.years.isNullOrEmpty(),
        value = decadeValue,
        options = decadeOptions,
        onOptionSelected = { option ->
            onUpdateFilter(
                state.filters.copy(
                    years = option.raw?.years?.let { persistentListOf(it.first, it.second) },
                ),
                true,
            )
        },
        enabled = enabled,
        modifier = modifier,
    )
}

@Composable
private fun RuntimeFilter(
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val runtimeValue: DropdownOption<GlobalFilterRuntime?> = state.filters.runtimes?.let { runtimes ->
        val span = GlobalFilterRuntime.entries.find { listOf(it.runtime.first, it.runtime.second) == runtimes }
        span?.let {
            DropdownOption(
                raw = it,
                displayString = when {
                    it == Runtime120Plus -> "${it.runtime.first}+"
                    else -> "${it.runtime.first}-${it.runtime.second}"
                },
            )
        }
    } ?: DropdownOption(
        raw = null,
        displayString = stringResource(R.string.option_text_all),
    )

    val runtimeOptions = buildList<DropdownOption<GlobalFilterRuntime?>> {
        add(
            DropdownOption(null, stringResource(R.string.option_text_all)),
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
        active = !state.filters.runtimes.isNullOrEmpty(),
        value = runtimeValue,
        options = runtimeOptions,
        onOptionSelected = { option ->
            onUpdateFilter(
                state.filters.copy(
                    runtimes = option.raw?.runtime?.let { persistentListOf(it.first, it.second) },
                ),
                true,
            )
        },
        enabled = enabled,
        modifier = modifier,
    )
}

@Composable
private fun CertificationFilter(
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val certificationValue: DropdownOption<Certification?> = state.filters.certifications?.firstOrNull()?.let { slug ->
        val certification = Certification.entries.find { it.slug == slug }
        certification?.let {
            DropdownOption(
                raw = it,
                displayString = stringResource(it.displayStringRes),
            )
        }
    } ?: DropdownOption(
        raw = null,
        displayString = stringResource(R.string.option_text_all),
    )

    val certificationOptions = buildList<DropdownOption<Certification?>> {
        add(
            DropdownOption(null, stringResource(R.string.option_text_all)),
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
        active = !state.filters.certifications.isNullOrEmpty(),
        value = certificationValue,
        options = certificationOptions,
        onOptionSelected = { option ->
            onUpdateFilter(
                state.filters.copy(
                    certifications = option.raw
                        ?.let { persistentListOf(it.slug) },
                ),
                true,
            )
        },
        enabled = enabled,
        modifier = modifier,
    )
}

@Composable
private fun RegionFilter(
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val regionValue: DropdownOption<Region?> = state.filters.countries?.let { countries ->
        val region = Region.entries.find { it.slug.split(',') == countries }
        region?.let {
            DropdownOption(
                raw = it,
                displayString = stringResource(it.displayStringRes),
            )
        }
    } ?: DropdownOption(
        raw = null,
        displayString = stringResource(R.string.option_text_all),
    )

    val regionOptions = buildList<DropdownOption<Region?>> {
        add(
            DropdownOption(null, stringResource(R.string.option_text_all)),
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
        active = !state.filters.countries.isNullOrEmpty(),
        value = regionValue,
        options = regionOptions,
        onOptionSelected = { option ->
            onUpdateFilter(
                state.filters.copy(
                    countries = option.raw?.slug?.split(',')?.toImmutableList(),
                ),
                true,
            )
        },
        enabled = enabled,
        modifier = modifier,
    )
}

@Composable
private fun RatingFilter(
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    ) {
        val rangeRatingValue = remember(state.filters.ratings) {
            mutableStateOf(
                state.filters.ratings?.let {
                    (it.getOrNull(0) ?: 0).toFloat()..(it.getOrNull(1) ?: 100).toFloat()
                } ?: (0f..100f),
            )
        }

        Text(
            text = stringResource(
                R.string.filter_label_ratings,
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
            enabled = enabled,
            valueRange = 0f..100f,
            steps = 19, // Step every 5%
            onValueChange = { range ->
                rangeRatingValue.value = range
            },
            onValueChangeFinished = {
                val value = rangeRatingValue.value
                onUpdateFilter(
                    state.filters.copy(
                        ratings = persistentListOf(value.start.toInt(), value.endInclusive.toInt()),
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
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
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
            val onCheckedChange: (Boolean) -> Unit = remember(state.filters) {
                {
                    onUpdateFilter(
                        state.filters.copy(
                            ignoreWatched = it,
                        ),
                        false,
                    )
                }
            }

            TraktSwitch(
                checked = state.filters.ignoreWatched,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
            Text(
                text = stringResource(R.string.header_hide_watched),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonTertiary,
                maxLines = 2,
                overflow = Ellipsis,
                modifier = Modifier.clickable(enabled = enabled) {
                    onCheckedChange(!state.filters.ignoreWatched)
                },
            )
        }

        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            val onCheckedChange: (Boolean) -> Unit = remember(state.filters) {
                {
                    onUpdateFilter(
                        state.filters.copy(
                            ignoreWatchlisted = it,
                        ),
                        false,
                    )
                }
            }

            TraktSwitch(
                checked = state.filters.ignoreWatchlisted,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
            Text(
                text = stringResource(R.string.header_hide_watchlisted),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonTertiary,
                maxLines = 2,
                overflow = Ellipsis,
                modifier = Modifier.clickable(enabled = enabled) {
                    onCheckedChange(!state.filters.ignoreWatchlisted)
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
        CreateSmartListFiltersSimpleView(state = CreateSmartListState())
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
        CreateSmartListFiltersSimpleView(
            state = CreateSmartListState(
                filters = SmartListFilters.Default.copy(
                    genres = persistentListOf(
                        MediaGenre.Action,
                        MediaGenre.Comedy,
                    ),
                    availability = persistentListOf(
                        Availability.AllDigitalReleases,
                    ),
                    years = persistentListOf(2020, 2029),
                    ratings = persistentListOf(60, 100),
                    ignoreWatched = true,
                ),
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewDisabled() {
    TraktTheme {
        CreateSmartListFiltersSimpleView(
            state = CreateSmartListState(
                creating = LoadingState.Loading,
                filters = SmartListFilters.Default.copy(
                    genres = persistentListOf(
                        MediaGenre.Action,
                        MediaGenre.Comedy,
                    ),
                    availability = persistentListOf(
                        Availability.AllDigitalReleases,
                    ),
                    years = persistentListOf(2020, 2029),
                    ratings = persistentListOf(60, 100),
                    ignoreWatched = true,
                ),
            ),
        )
    }
}
